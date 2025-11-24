package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxResponse {
    
    private Integer id;
    private Integer tenantId;
    private String code;
    private String description;
    private BigDecimal value;
    private Boolean isDefault;  // Removed redundant defaultFlag string
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;  // Removed redundant status string
    private Boolean isCurrentlyValid;
    private String createdBy;
    private LocalDateTime created;
    private String updatedBy;
    private LocalDateTime updated;  // Removed redundant modified/modifiedBy
    
}
