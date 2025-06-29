package com.inventrik.digitalestore.domain.download;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "digital_downloads")
@IdClass(DigitalDownload.DigitalDownloadPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalDownload {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "download_id")
    private Long downloadId;
    
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;
    
    @Column(name = "download_date", nullable = false)
    private LocalDateTime downloadDate;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
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
        downloadDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public static class DigitalDownloadPK implements Serializable {
        private Integer tenantId;
        private Long downloadId;
        
        public DigitalDownloadPK() {}
        
        public DigitalDownloadPK(Integer tenantId, Long downloadId) {
            this.tenantId = tenantId;
            this.downloadId = downloadId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getDownloadId() { return downloadId; }
        public void setDownloadId(Long downloadId) { this.downloadId = downloadId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DigitalDownloadPK)) return false;
            DigitalDownloadPK that = (DigitalDownloadPK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(downloadId, that.downloadId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, downloadId);
        }
    }
}