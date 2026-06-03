package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Review.ReviewPK> {
    
    List<Review> findByTenantIdAndProductIdAndStatusOrderByReviewDateDesc(
        Integer tenantId, String productId, String status);

    List<Review> findByTenantIdAndUserIdAndStatusOrderByReviewDateDesc(
        Integer tenantId, String userId, String status);

    List<Review> findByTenantIdAndRatingGreaterThanEqualAndStatusOrderByReviewDateDesc(
        Integer tenantId, Integer rating, String status);

    Optional<Review> findByTenantIdAndUserIdAndProductIdAndStatus(
        Integer tenantId, String userId, String productId, String status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tenantId = :tenantId AND r.productId = :productId AND r.status = '0'")
    Double findAverageRatingByProduct(@Param("tenantId") Integer tenantId, @Param("productId") String productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tenantId = :tenantId AND r.productId = :productId AND r.status = '0'")
    Long countReviewsByProduct(@Param("tenantId") Integer tenantId, @Param("productId") String productId);

    @Query("SELECT r.productId AS productId, AVG(r.rating) AS averageRating, COUNT(r) AS totalReviews " +
           "FROM Review r WHERE r.tenantId = :tenantId AND r.productId IN (:productIds) AND r.status = '0' " +
           "GROUP BY r.productId")
    List<ProductRatingView> findRatingsByProductIds(@Param("tenantId") Integer tenantId,
                                                    @Param("productIds") List<String> productIds);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tenantId = :tenantId AND r.productId = :productId AND r.rating = :rating AND r.status = '0'")
    Long countReviewsByProductAndRating(@Param("tenantId") Integer tenantId, @Param("productId") String productId, @Param("rating") Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.tenantId = :tenantId AND r.verified = true AND r.status = '0' ORDER BY r.reviewDate DESC")
    List<Review> findVerifiedReviews(@Param("tenantId") Integer tenantId);
    
    @Query("SELECT MAX(r.reviewId) FROM Review r WHERE r.tenantId = :tenantId")
    String findMaxReviewId(@Param("tenantId") Integer tenantId);
} 