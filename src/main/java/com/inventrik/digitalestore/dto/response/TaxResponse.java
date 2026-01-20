package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TaxResponse {
    private Integer tenantId;
    private String taxId;
    private String code;
    private String description;
    private BigDecimal value;
    private String defaultFlag;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
}
