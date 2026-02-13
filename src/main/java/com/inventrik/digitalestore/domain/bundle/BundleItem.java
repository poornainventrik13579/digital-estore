package com.inventrik.digitalestore.domain.bundle;

import com.inventrik.digitalestore.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bundleitems")
@IdClass(BundleItem.BundleItemPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleItem {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "bundle_item_id")
    private String bundleItemId;

    @Column(name = "bundle_id", nullable = false)
    private String bundleId;

    @Column(name = "product_id", nullable = false)
    private String productId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "bundle_id", referencedColumnName = "bundle_id", insertable = false, updatable = false)
    })
    private ProductBundle productBundle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    })
    private Product product;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public static class BundleItemPK implements Serializable {
        private Integer tenantId;
        private String bundleItemId;

        public BundleItemPK() {}

        public BundleItemPK(Integer tenantId, String bundleItemId) {
            this.tenantId = tenantId;
            this.bundleItemId = bundleItemId;
        }

        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public String getBundleItemId() { return bundleItemId; }
        public void setBundleItemId(String bundleItemId) { this.bundleItemId = bundleItemId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BundleItemPK)) return false;
            BundleItemPK that = (BundleItemPK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(bundleItemId, that.bundleItemId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, bundleItemId);
        }
    }
} 