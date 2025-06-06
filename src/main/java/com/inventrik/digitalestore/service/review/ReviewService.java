package com.inventrik.digitalestore.service.review;

import com.inventrik.digitalestore.dto.request.ReviewRequest;
import com.inventrik.digitalestore.dto.response.ProductRatingResponse;
import com.inventrik.digitalestore.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    
    ReviewResponse createReview(Integer tenantId, String username, ReviewRequest reviewRequest);
    
    List<ReviewResponse> getProductReviews(Integer tenantId, Long productId);
    
    List<ReviewResponse> getUserReviews(Integer tenantId, Long userId);
    
    ReviewResponse getReview(Integer tenantId, Long reviewId);
    
    ReviewResponse updateReview(Integer tenantId, Long reviewId, ReviewRequest reviewRequest, String username);
    
    void deleteReview(Integer tenantId, Long reviewId, String username);
    
    ProductRatingResponse getProductRating(Integer tenantId, Long productId);
    
    List<ReviewResponse> getVerifiedReviews(Integer tenantId);
    
    ReviewResponse verifyReview(Integer tenantId, Long reviewId, String username);
} 