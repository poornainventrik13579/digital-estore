package com.inventrik.digitalestore.service.review;

import com.inventrik.digitalestore.dto.request.ReviewRequest;
import com.inventrik.digitalestore.dto.response.ProductRatingResponse;
import com.inventrik.digitalestore.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(Integer tenantId, String username, ReviewRequest reviewRequest);

    List<ReviewResponse> getProductReviews(Integer tenantId, String productId);

    List<ReviewResponse> getUserReviews(Integer tenantId, String userId);

    ReviewResponse getReview(Integer tenantId, String reviewId);

    ReviewResponse updateReview(Integer tenantId, String reviewId, ReviewRequest reviewRequest, String username);

    void deleteReview(Integer tenantId, String reviewId, String username);

    ProductRatingResponse getProductRating(Integer tenantId, String productId);

    List<ReviewResponse> getVerifiedReviews(Integer tenantId);

    ReviewResponse verifyReview(Integer tenantId, String reviewId, String username);
} 