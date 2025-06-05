package com.inventrik.digitalestore.domain.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "DigitalProductDetails")
@IdClass(DigitalProductDetails.DigitalProductDetailsPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalProductDetails {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;
    
    @Column(name = "file_size")
    private Integer fileSize;
    
    @Column(name = "file_format", length = 20)
    private String fileFormat;
    
    @Column(name = "license_info", columnDefinition = "TEXT")
    private String licenseInfo;
    
    @Column(name = "version", length = 20)
    private String version;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public static class DigitalProductDetailsPK implements Serializable {
        private Integer tenantId;
        private Long productId;
        
        public DigitalProductDetailsPK() {}
        
        public DigitalProductDetailsPK(Integer tenantId, Long productId) {
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
            if (!(o instanceof DigitalProductDetailsPK)) return false;
            DigitalProductDetailsPK that = (DigitalProductDetailsPK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(productId, that.productId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, productId);
        }
    }
}