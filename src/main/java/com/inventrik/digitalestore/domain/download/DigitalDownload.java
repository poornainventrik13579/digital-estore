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
    @JoinColumn(name = "order_item_id", referencedColumnName = "order_item_id", insertable = false, updatable = false)
    private OrderItem orderItem;
    
    @Column(name = "download_date", nullable = false)
    private LocalDateTime downloadDate;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status; // 0:ACTIVE, -1 INACTIVE
    
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