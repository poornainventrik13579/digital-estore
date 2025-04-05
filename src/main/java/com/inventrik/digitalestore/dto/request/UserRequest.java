package com.inventrik.digitalestore.dto.request;

import com.inventrik.digitalestore.domain.user.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    
    @Schema(description = "Username", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Schema(description = "First name", example = "John", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;
    
    @Schema(description = "Profile image URL", example = "https://example.com/profile.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 256, message = "Image URL must be less than 256 characters")
    private String image;
    
    @Schema(description = "Phone number", example = "+1-555-123-4567", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number is required")
    @Size(max = 100, message = "Phone number must be less than 100 characters")
    private String phone;
    
    @Schema(description = "Email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @Schema(description = "Password (min 8 characters)", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
    
    @Schema(description = "User type (INDIVIDUAL or COMPANY)", example = "INDIVIDUAL", defaultValue = "INDIVIDUAL")
    private UserType userType = UserType.INDIVIDUAL;
    
    // Company specific fields (only required when userType is COMPANY)
    @Schema(description = "Company name (required for COMPANY users)", example = "Acme Corporation")
    private String companyName;
    
    @Schema(description = "Company registration number", example = "REG123456")
    private String companyRegistrationNumber;
    
    @Schema(description = "Company address line 1", example = "123 Main St")
    private String companyAddress1;
    
    @Schema(description = "Company address line 2", example = "Suite 100")
    private String companyAddress2;
    
    @Schema(description = "Company country", example = "United States")
    private String companyCountry;
    
    @Schema(description = "Company postal/ZIP code", example = "12345")
    private String companyPincode;
    
    @Schema(description = "Tax ID or VAT number", example = "TAX987654321")
    private String taxId;
}