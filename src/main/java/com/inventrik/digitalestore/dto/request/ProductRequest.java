package com.inventrik.digitalestore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must be less than 100 characters")
    private String productName;
    
    private String description;
    
    @NotNull(message = "Default price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal defaultPrice;
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String defaultCurrency;
    
    @Size(max = 256, message = "Image1 URL must be less than 256 characters")
    private String image1Url;
    
    @Size(max = 256, message = "Image2 URL must be less than 256 characters")
    private String image2Url;
    
    @Size(max = 256, message = "Image3 URL must be less than 256 characters")
    private String image3Url;
    
    @Size(max = 256, message = "Image4 URL must be less than 256 characters")
    private String image4Url;
    
    @Size(max = 256, message = "Image5 URL must be less than 256 characters")
    private String image5Url;
    
    @Size(max = 256, message = "Banner URL must be less than 256 characters")
    private String banner;
    
    @Size(max = 256, message = "Thumbnail URL must be less than 256 characters")
    private String thumbnail;
    
    private String metadata;

    private String categoryId;
}
