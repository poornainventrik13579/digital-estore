package com.inventrik.digitalestore.domain.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DigitalProductDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalProductDetails {
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    })
    private Product product;
    
    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;
    
    @Column(name = "file_size")
    private Integer fileSize; // Size in KB/MB as per design brief
    
    @Column(name = "file_format", length = 20)
    private String fileFormat; // e.g., PDF, MP3, MP4, etc.
    
    @Column(name = "license_info", columnDefinition = "TEXT")
    private String licenseInfo; // Terms or license keys
    
    @Column(name = "version", length = 20)
    private String version;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status; // -1 INACTIVE, 0 ACTIVE
    
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
}