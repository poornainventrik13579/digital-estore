package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {
    
    @Schema(description = "Order status", example = "Completed", allowableValues = {"Pending", "Processing", "Completed", "Cancelled", "Refunded", "Partially Refunded"})
    @Size(max = 20, message = "Status must be less than 20 characters")
    private String status;
}