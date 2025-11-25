package com.inventrik.digitalestore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TaxRequest {

    @NotBlank(message = "Code is required")
    private String code;

    private String description;

    @NotNull(message = "Value is required")
    private BigDecimal value;

    private String defaultFlag;
    private LocalDate startDate;
    private LocalDate endDate;
}
