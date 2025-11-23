package com.inventrik.digitalestore.domain.product;

import com.inventrik.digitalestore.domain.category.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "products")
@IdClass(Product.ProductPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "default_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultPrice;
    
    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;
    
    @Column(name = "image1_url", length = 256)
    private String image1Url;
    
    @Column(name = "image2_url", length = 256)
    private String image2Url;
    
    @Column(name = "image3_url", length = 256)
    private String image3Url;
    
    @Column(name = "image4_url", length = 256)
    private String image4Url;
    
    @Column(name = "image5_url", length = 256)
    private String image5Url;
    
    @Column(name = "banner", length = 256)
    private String banner;
    
    @Column(name = "thumbnail", length = 256)
    private String thumbnail;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status;
    
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "category_id", referencedColumnName = "category_id", insertable = false, updatable = false)
    })
    private Category category;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public static class ProductPK implements Serializable {
        private Integer tenantId;
        private Long productId;
        
        public ProductPK() {}
        
        public ProductPK(Integer tenantId, Long productId) {
            this.tenantId = tenantId;
            this.productId = productId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProductPK)) return false;
            ProductPK productPK = (ProductPK) o;
            return Objects.equals(tenantId, productPK.tenantId) && Objects.equals(productId, productPK.productId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, productId);
        }
    }
}