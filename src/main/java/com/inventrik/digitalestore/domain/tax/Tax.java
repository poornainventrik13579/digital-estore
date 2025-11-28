package com.inventrik.digitalestore.domain.tax;

import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "taxes")
@IdClass(Tax.TaxPK.class)
@Data
public class Tax {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Id
    @Column(name = "tax_id")
    private Long taxId;

    @Column(name = "code", nullable = false, length = 255)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "default_flag", length = 2)
    private String defaultFlag = "N";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false, length = 2)
    private String status;

    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;

    @Column(name = "updated")
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

    public static class TaxPK implements Serializable {
        private Integer tenantId;
        private Long taxId;

        public TaxPK() {}

        public TaxPK(Integer tenantId, Long taxId) {
            this.tenantId = tenantId;
            this.taxId = taxId;
        }

        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getTaxId() { return taxId; }
        public void setTaxId(Long taxId) { this.taxId = taxId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TaxPK)) return false;
            TaxPK that = (TaxPK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(taxId, that.taxId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, taxId);
        }
    }
}
