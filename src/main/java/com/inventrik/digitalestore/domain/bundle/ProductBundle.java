package com.inventrik.digitalestore.domain.bundle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "productbundles")
@IdClass(ProductBundle.ProductBundlePK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundle {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "bundle_id")
    private String bundleId;
    
    @Column(name = "bundle_name", nullable = false, length = 100)
    private String bundleName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "bundle_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal bundlePrice;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "0";
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @OneToMany(mappedBy = "productBundle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BundleItem> bundleItems = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public void addBundleItem(BundleItem bundleItem) {
        bundleItems.add(bundleItem);
        bundleItem.setProductBundle(this);
    }
    
    public void removeBundleItem(BundleItem bundleItem) {
        bundleItems.remove(bundleItem);
        bundleItem.setProductBundle(null);
    }
    
    public static class ProductBundlePK implements Serializable {
        private Integer tenantId;
        private String bundleId;

        public ProductBundlePK() {}

        public ProductBundlePK(Integer tenantId, String bundleId) {
            this.tenantId = tenantId;
            this.bundleId = bundleId;
        }

        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public String getBundleId() { return bundleId; }
        public void setBundleId(String bundleId) { this.bundleId = bundleId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProductBundlePK)) return false;
            ProductBundlePK that = (ProductBundlePK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(bundleId, that.bundleId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, bundleId);
        }
    }
} 