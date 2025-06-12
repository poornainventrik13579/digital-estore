package com.inventrik.digitalestore.service.review;

import com.inventrik.digitalestore.domain.review.Review;
import com.inventrik.digitalestore.dto.request.ReviewRequest;
import com.inventrik.digitalestore.dto.response.ProductRatingResponse;
import com.inventrik.digitalestore.dto.response.ReviewResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.exception.ValidationException;
import com.inventrik.digitalestore.repository.ReviewRepository;
import com.inventrik.digitalestore.service.order.OrderService;
import com.inventrik.digitalestore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final OrderService orderService;
    
    @Override
    public ReviewResponse createReview(Integer tenantId, String username, ReviewRequest reviewRequest) {
        log.info("Creating review for product {} by user {}", reviewRequest.getProductId(), username);
        
        var user = userService.findByUsername(username);
        
        // Check if user already reviewed this product
        Optional<Review> existingReview = reviewRepository.findByTenantIdAndUserIdAndProductIdAndStatus(
            tenantId, user.getUserId(), reviewRequest.getProductId(), "0");
        
        if (existingReview.isPresent()) {
            throw new ValidationException("User has already reviewed this product");
        }
        
        // Verify user purchased this product
        boolean hasPurchased = orderService.hasUserPurchasedProduct(tenantId, user.getUserId(), reviewRequest.getProductId());
        
        Long reviewId = generateReviewId(tenantId);
        
        Review review = new Review();
        review.setTenantId(tenantId);
        review.setReviewId(reviewId);
        review.setProductId(reviewRequest.getProductId());
        review.setUserId(user.getUserId());
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setVerified(hasPurchased);
        review.setStatus("0");
        review.setCreatedBy(username.length() > 2 ? username.substring(0, 2) : username);
        review.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
        
        Review savedReview = reviewRepository.save(review);
        
        log.info("Review created successfully with ID: {}", savedReview.getReviewId());
        return mapToResponse(savedReview, user.getUsername());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Integer tenantId, Long productId) {
        log.info("Fetching reviews for product: {}", productId);
        
        List<Review> reviews = reviewRepository.findByTenantIdAndProductIdAndStatusOrderByReviewDateDesc(
            tenantId, productId, "0");
        
        return reviews.stream()
            .map(review -> {
                var user = userService.getUser(tenantId, review.getUserId());
                return mapToResponse(review, user.getUsername());
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(Integer tenantId, Long userId) {
        log.info("Fetching reviews for user: {}", userId);
        
        List<Review> reviews = reviewRepository.findByTenantIdAndUserIdAndStatusOrderByReviewDateDesc(
            tenantId, userId, "0");
        
        var user = userService.getUser(tenantId, userId);
        
        return reviews.stream()
            .map(review -> mapToResponse(review, user.getUsername()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Integer tenantId, Long reviewId) {
        log.info("Fetching review: {}", reviewId);
        
        Review review = reviewRepository.findById(new Review.ReviewPK(tenantId, reviewId))
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        var user = userService.getUser(tenantId, review.getUserId());
        return mapToResponse(review, user.getUsername());
    }
    
    @Override
    public ReviewResponse updateReview(Integer tenantId, Long reviewId, ReviewRequest reviewRequest, String username) {
        log.info("Updating review: {} by user: {}", reviewId, username);
        
        Review review = reviewRepository.findById(new Review.ReviewPK(tenantId, reviewId))
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        var user = userService.findByUsername(username);
        
        // Only allow user to update their own review
        if (!review.getUserId().equals(user.getUserId())) {
            throw new ValidationException("User can only update their own reviews");
        }
        
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setUpdatedBy(username);
        
        Review updatedReview = reviewRepository.save(review);
        
        log.info("Review updated successfully: {}", reviewId);
        return mapToResponse(updatedReview, user.getUsername());
    }
    
    @Override
    public void deleteReview(Integer tenantId, Long reviewId, String username) {
        log.info("Deleting review: {} by user: {}", reviewId, username);
        
        Review review = reviewRepository.findById(new Review.ReviewPK(tenantId, reviewId))
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        var user = userService.findByUsername(username);
        
        // Only allow user to delete their own review
        if (!review.getUserId().equals(user.getUserId())) {
            throw new ValidationException("User can only delete their own reviews");
        }
        
        review.setStatus("-1");
        review.setUpdatedBy(username);
        
        reviewRepository.save(review);
        
        log.info("Review deleted successfully: {}", reviewId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductRatingResponse getProductRating(Integer tenantId, Long productId) {
        log.info("Calculating rating statistics for product: {}", productId);
        
        Double averageRating = reviewRepository.findAverageRatingByProduct(tenantId, productId);
        Long totalReviews = reviewRepository.countReviewsByProduct(tenantId, productId);
        
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            Long count = reviewRepository.countReviewsByProductAndRating(tenantId, productId, rating);
            ratingDistribution.put(rating, count);
        }
        
        ProductRatingResponse response = new ProductRatingResponse(productId, averageRating, totalReviews);
        response.setRatingDistribution(ratingDistribution);
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getVerifiedReviews(Integer tenantId) {
        log.info("Fetching verified reviews for tenant: {}", tenantId);
        
        List<Review> reviews = reviewRepository.findVerifiedReviews(tenantId);
        
        return reviews.stream()
            .map(review -> {
                var user = userService.getUser(tenantId, review.getUserId());
                return mapToResponse(review, user.getUsername());
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public ReviewResponse verifyReview(Integer tenantId, Long reviewId, String username) {
        log.info("Verifying review: {} by admin: {}", reviewId, username);
        
        Review review = reviewRepository.findById(new Review.ReviewPK(tenantId, reviewId))
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        review.setVerified(true);
        review.setUpdatedBy(username);
        
        Review verifiedReview = reviewRepository.save(review);
        
        var user = userService.getUser(tenantId, review.getUserId());
        
        log.info("Review verified successfully: {}", reviewId);
        return mapToResponse(verifiedReview, user.getUsername());
    }
    
    private Long generateReviewId(Integer tenantId) {
        Long maxId = reviewRepository.findMaxReviewId(tenantId);
        return maxId != null ? maxId + 1 : 1000000001L;
    }
    

    
    private ReviewResponse mapToResponse(Review review, String username) {
        return new ReviewResponse(
            review.getReviewId(),
            review.getProductId(),
            review.getUserId(),
            username,
            review.getRating(),
            review.getComment(),
            review.getReviewDate(),
            review.getVerified(),
            review.getStatus()
        );
    }
} 