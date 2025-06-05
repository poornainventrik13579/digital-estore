package com.inventrik.digitalestore.domain.download;

import com.inventrik.digitalestore.domain.order.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DigitalDownloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalDownload {
    
    @Id
    @Column(name = "download_id")
    private Long downloadId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "order_item_id", referencedColumnName = "order_item_id", insertable = false, updatable = false)
    })
    private OrderItem orderItem;
    
    @Column(name = "download_date", nullable = false)
    private LocalDateTime downloadDate;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "download_token", length = 100, unique = true)
    private String downloadToken;
    
    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;
    
    @Column(name = "file_size_downloaded")
    private Long fileSizeDownloaded;
    
    @Column(name = "download_status", length = 20)
    private String downloadStatus; // INITIATED, IN_PROGRESS, COMPLETED, FAILED
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "0"; // 0: Active, -1: Inactive
    
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
        downloadDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}