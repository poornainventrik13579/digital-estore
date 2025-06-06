package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalProductDetailsRequest {
    
    @Schema(description = "Product ID", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Schema(description = "File URL or path", example = "/files/product123.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "File URL is required")
    @Size(max = 255, message = "File URL must be less than 255 characters")
    private String fileUrl;
    
    @Schema(description = "File size in KB/MB", example = "1024")
    @Min(value = 0, message = "File size must be positive")
    private Integer fileSize;
    
    @Schema(description = "File format", example = "PDF")
    @Size(max = 20, message = "File format must be less than 20 characters")
    private String fileFormat;
    
    @Schema(description = "License information", example = "Single user license")
    private String licenseInfo;
    
    @Schema(description = "Version", example = "1.0")
    @Size(max = 20, message = "Version must be less than 20 characters")
    private String version;
    
    @Schema(description = "Status (-1 for inactive, 0 for active)", example = "0", allowableValues = {"0", "-1"})
    private String status;
}