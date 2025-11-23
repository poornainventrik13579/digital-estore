package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.ProductRatingResponse;
import com.inventrik.digitalestore.dto.response.ReviewResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.service.review.ReviewService;
import com.inventrik.digitalestore.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/tenants/{tenantId}/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Reviews", description = "Public APIs for viewing reviews without authentication")
public class PublicReviewController {
    
    private final ReviewService reviewService;
    private final TenantService tenantService;
    
    private boolean validateTenant(Integer tenantId) {
        try {
            var tenant = tenantService.getTenant(tenantId);
            return "A".equals(tenant.getStatus());
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        if (!validateTenant(tenantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        log.info("Fetching reviews for product: {}", productId);
        
        List<ReviewResponse> reviews = reviewService.getProductReviews(tenantId, productId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/product/{productId}/rating")
    @Operation(summary = "Get product rating statistics")
    public ResponseEntity<ProductRatingResponse> getProductRating(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        if (!validateTenant(tenantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        log.info("Fetching rating statistics for product: {}", productId);
        
        ProductRatingResponse rating = reviewService.getProductRating(tenantId, productId);
        return ResponseEntity.ok(rating);
    }
    
    @GetMapping("/verified")
    @Operation(summary = "Get verified reviews")
    public ResponseEntity<List<ReviewResponse>> getVerifiedReviews(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId) {
        
        if (!validateTenant(tenantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        log.info("Fetching verified reviews for tenant: {}", tenantId);
        
        List<ReviewResponse> reviews = reviewService.getVerifiedReviews(tenantId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Review ID") @PathVariable Long reviewId) {
        
        if (!validateTenant(tenantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        log.info("Fetching review: {}", reviewId);
        
        ReviewResponse review = reviewService.getReview(tenantId, reviewId);
        return ResponseEntity.ok(review);
    }
} 