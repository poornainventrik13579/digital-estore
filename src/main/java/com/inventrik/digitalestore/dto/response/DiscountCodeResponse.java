package com.inventrik.digitalestore.dto.response;

import com.inventrik.digitalestore.domain.discount.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCodeResponse {
    
    @Schema(description = "Discount ID", example = "12345")
    private Long discountId;
    
    @Schema(description = "Tenant ID", example = "1")
    private Integer tenantId;
    
    @Schema(description = "Discount code", example = "SAVE20")
    private String code;
    
    @Schema(description = "Discount type", example = "PERCENTAGE")
    private DiscountType discountType;
    
    @Schema(description = "Discount value", example = "20.00")
    private BigDecimal discountValue;
    
    @Schema(description = "Minimum order amount", example = "50.00")
    private BigDecimal minOrderAmount;
    
    @Schema(description = "Maximum uses allowed", example = "100")
    private Integer maxUses;
    
    @Schema(description = "Number of times used", example = "25")
    private Integer usedCount;
    
    @Schema(description = "Valid from date", example = "2024-01-01T00:00:00")
    private LocalDateTime validFrom;

    @Schema(description = "Valid until date", example = "2024-12-31T23:59:59")
    private LocalDateTime validUntil;
    
    @Schema(description = "Status", example = "0")
    private String status;
    
    @Schema(description = "Created date", example = "2024-01-01T10:30:00")
    private LocalDateTime created;
    
    @Schema(description = "Updated date", example = "2024-01-01T15:45:00")
    private LocalDateTime updated;
    
    @Schema(description = "Is currently active", example = "true")
    private boolean active;
    
    @Schema(description = "Is currently valid", example = "true")
    private boolean valid;
    
    @Schema(description = "Has uses remaining", example = "true")
    private boolean hasUsesRemaining;
    
    @Schema(description = "Remaining uses", example = "75")
    private Integer remainingUses;
} 