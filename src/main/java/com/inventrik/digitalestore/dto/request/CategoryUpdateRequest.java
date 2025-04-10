package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    
    @Schema(description = "Category name", example = "Electronics")
    @Size(max = 50, message = "Category name must be less than 50 characters")
    private String categoryName;
    
    @Schema(description = "Category description", example = "Electronic devices and gadgets")
    private String description;
    
    @Schema(description = "Category status (0 for active, -1 for inactive)", example = "0", allowableValues = {"0", "-1"})
    private String status;
}