package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.ReviewRequest;
import com.inventrik.digitalestore.dto.response.ProductRatingResponse;
import com.inventrik.digitalestore.dto.response.ReviewResponse;
import com.inventrik.digitalestore.service.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews", description = "Review management operations")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create a new review", description = "Create a review for a product")
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            Authentication authentication) {
        
        log.info("Creating review for product {} by user {}", reviewRequest.getProductId(), authentication.getName());
        
        ReviewResponse response = reviewService.createReview(tenantId, authentication.getName(), reviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Get all reviews for a specific product")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        
        log.info("Fetching reviews for product: {}", productId);
        
        List<ReviewResponse> reviews = reviewService.getProductReviews(tenantId, productId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user reviews", description = "Get all reviews by a specific user")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        
        log.info("Fetching reviews for user: {}", userId);
        
        List<ReviewResponse> reviews = reviewService.getUserReviews(tenantId, userId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Get a specific review by its ID")
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId) {
        
        log.info("Fetching review: {}", reviewId);
        
        ReviewResponse review = reviewService.getReview(tenantId, reviewId);
        return ResponseEntity.ok(review);
    }
    
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update review", description = "Update an existing review")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            Authentication authentication) {
        
        log.info("Updating review {} by user {}", reviewId, authentication.getName());
        
        ReviewResponse response = reviewService.updateReview(tenantId, reviewId, reviewRequest, authentication.getName());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId,
            Authentication authentication) {
        
        log.info("Deleting review {} by user {}", reviewId, authentication.getName());
        
        reviewService.deleteReview(tenantId, reviewId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/product/{productId}/rating")
    @Operation(summary = "Get product rating statistics", description = "Get average rating and distribution for a product")
    public ResponseEntity<ProductRatingResponse> getProductRating(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        
        log.info("Fetching rating statistics for product: {}", productId);
        
        ProductRatingResponse rating = reviewService.getProductRating(tenantId, productId);
        return ResponseEntity.ok(rating);
    }
    
    @GetMapping("/verified")
    @Operation(summary = "Get verified reviews", description = "Get all verified reviews")
    public ResponseEntity<List<ReviewResponse>> getVerifiedReviews(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId) {
        
        log.info("Fetching verified reviews for tenant: {}", tenantId);
        
        List<ReviewResponse> reviews = reviewService.getVerifiedReviews(tenantId);
        return ResponseEntity.ok(reviews);
    }
    
    @PutMapping("/{reviewId}/verify")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Verify review", description = "Mark a review as verified (admin only)")
    public ResponseEntity<ReviewResponse> verifyReview(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId,
            Authentication authentication) {
        
        log.info("Verifying review {} by admin {}", reviewId, authentication.getName());
        
        ReviewResponse response = reviewService.verifyReview(tenantId, reviewId, authentication.getName());
        return ResponseEntity.ok(response);
    }
} 