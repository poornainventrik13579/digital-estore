package com.inventrik.digitalestore.repository;

/**
 * Projection for the batch rating query. One row per product, computed in a single
 * grouped SQL statement so the product list no longer needs an N+1 call per product.
 */
public interface ProductRatingView {
    String getProductId();
    Double getAverageRating();
    Long getTotalReviews();
}
