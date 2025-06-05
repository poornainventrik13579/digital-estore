package com.inventrik.digitalestore.domain.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Reviews")
@IdClass(Review.ReviewPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "review_id")
    private Long reviewId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "review_date", nullable = false)
    private LocalDateTime reviewDate;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "0";
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 2)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
        reviewDate = LocalDateTime.now();
        if (verified == null) {
            verified = false;
        }
        if (status == null) {
            status = "0";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return "0".equals(status);
    }
    
    public static class ReviewPK implements Serializable {
        private Integer tenantId;
        private Long reviewId;
        
        public ReviewPK() {}
        
        public ReviewPK(Integer tenantId, Long reviewId) {
            this.tenantId = tenantId;
            this.reviewId = reviewId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getReviewId() { return reviewId; }
        public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReviewPK)) return false;
            ReviewPK reviewPK = (ReviewPK) o;
            return Objects.equals(tenantId, reviewPK.tenantId) && Objects.equals(reviewId, reviewPK.reviewId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, reviewId);
        }
    }
} 