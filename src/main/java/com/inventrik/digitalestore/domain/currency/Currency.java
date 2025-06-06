package com.inventrik.digitalestore.domain.currency;

import com.inventrik.digitalestore.domain.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Currencies")
@IdClass(CurrencyId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Currency extends AuditableEntity {
    
    @Id
    @Column(name = "tenant_id")
    private Integer tenantId;
    
    @Id
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "currency_name", length = 50, nullable = false)
    private String currencyName;
    
    @Column(name = "is_default", length = 1, nullable = false)
    private String isDefault;
    
    @Column(name = "exchange_rate", precision = 10, scale = 4, nullable = false)
    private BigDecimal exchangeRate;
    
    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;
    
    @Column(name = "status", length = 2, nullable = false)
    private String status;
} 