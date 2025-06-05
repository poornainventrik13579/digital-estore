package com.inventrik.digitalestore.domain.order;

import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.domain.download.DigitalDownload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "OrderItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @Column(name = "order_item_id")
    private Long orderItemId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false)
    })
    @ToString.Exclude
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    })
    private Product product;
    
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
    
    @Column(name = "license_key", length = 100)
    private String licenseKey;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "0"; // Active status
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 2)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    // NEW ADDITION: Digital Downloads relationship
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DigitalDownload> downloads = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    // NEW ADDITION: Helper method to check if order item has downloads
    public boolean hasDownloads() {
        return downloads != null && !downloads.isEmpty();
    }
    
    // NEW ADDITION: Helper method to get completed downloads count
    public long getCompletedDownloadsCount() {
        return downloads.stream()
                .filter(download -> "COMPLETED".equals(download.getDownloadStatus()))
                .count();
    }
    
    // NEW ADDITION: Helper method to get total downloads count
    public long getTotalDownloadsCount() {
        return downloads != null ? downloads.size() : 0;
    }
    
    // NEW ADDITION: Helper method to check if item is digital
    public boolean isDigitalProduct() {
        return product != null && product.hasDigitalDetails();
    }
    
    // NEW ADDITION: Helper method to get remaining downloads
    public int getRemainingDownloads() {
        if (product == null || product.getDigitalDetails() == null) {
            return 0;
        }
        
        Integer downloadLimit = product.getDigitalDetails().getDownloadLimit();
        if (downloadLimit == null) {
            return -1; // Unlimited
        }
        
        long completedDownloads = getCompletedDownloadsCount();
        return Math.max(0, downloadLimit - (int) completedDownloads);
    }
    
    // NEW ADDITION: Helper method to check if downloads are expired
    public boolean areDownloadsExpired() {
        if (product == null || product.getDigitalDetails() == null) {
            return false;
        }
        
        Integer expiryDays = product.getDigitalDetails().getExpiryDays();
        if (expiryDays == null) {
            return false; // No expiry
        }
        
        LocalDateTime expiryDate = created.plusDays(expiryDays);
        return LocalDateTime.now().isAfter(expiryDate);
    }
}