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
    @Size(max = 500, message = "File URL must be less than 500 characters")
    private String fileUrl;
    
    @Schema(description = "File size in bytes", example = "1048576")
    @Min(value = 0, message = "File size must be positive")
    private Long fileSize;
    
    @Schema(description = "File format", example = "PDF")
    @Size(max = 50, message = "File format must be less than 50 characters")
    private String fileFormat;
    
    @Schema(description = "License information", example = "Single user license")
    private String licenseInfo;
    
    @Schema(description = "Version", example = "1.0")
    @Size(max = 20, message = "Version must be less than 20 characters")
    private String version;
    
    @Schema(description = "Download limit (null for unlimited)", example = "5")
    @Min(value = 1, message = "Download limit must be at least 1")
    private Integer downloadLimit;
    
    @Schema(description = "Expiry days after purchase", example = "30")
    @Min(value = 1, message = "Expiry days must be at least 1")
    private Integer expiryDays;
    
    @Schema(description = "File hash for integrity", example = "sha256hash")
    @Size(max = 64, message = "File hash must be less than 64 characters")
    private String fileHash;
    
    @Schema(description = "Status (0 for active, -1 for inactive)", example = "0", allowableValues = {"0", "-1"})
    private String status;
}