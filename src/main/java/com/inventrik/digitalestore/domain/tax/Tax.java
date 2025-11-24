package com.inventrik.digitalestore.domain.tax;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "taxes")
@IdClass(Tax.TaxPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tax {
    
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "code", nullable = false, length = 255)
    private String code;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;
    
    @Column(name = "default_flag", nullable = false, length = 2)
    private String defaultFlag = "N";
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "modified", nullable = false)
    private LocalDateTime modified;
    
    @Column(name = "modified_by", nullable = false, length = 255)
    private String modifiedBy;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "A";
    
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
        modified = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
        modified = LocalDateTime.now();
    }
    
    public boolean isDefault() {
        return "Y".equals(defaultFlag);
    }
    
    public void setAsDefault() {
        this.defaultFlag = "Y";
    }
    
    public boolean isActive() {
        return "A".equals(status);
    }
    
    public boolean isValidForDate(LocalDate date) {
        if (!isActive()) {
            return false;
        }
        
        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);
        
        return afterStart && beforeEnd;
    }
    
    public boolean isCurrentlyValid() {
        return isValidForDate(LocalDate.now());
    }
    
    public BigDecimal calculateTaxAmount(BigDecimal baseAmount) {
        if (!isCurrentlyValid() || baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        return baseAmount.multiply(value).divide(new BigDecimal(100));
    }
    
    public static class TaxPK implements Serializable {
        private Integer id;
        private Integer tenantId;
        
        public TaxPK() {}
        
        public TaxPK(Integer id, Integer tenantId) {
            this.id = id;
            this.tenantId = tenantId;
        }
        
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TaxPK)) return false;
            TaxPK taxPK = (TaxPK) o;
            return Objects.equals(id, taxPK.id) && Objects.equals(tenantId, taxPK.tenantId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id, tenantId);
        }
    }
}
