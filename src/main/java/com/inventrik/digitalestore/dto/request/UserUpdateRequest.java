package com.inventrik.digitalestore.dto.request;

import com.inventrik.digitalestore.domain.user.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @Schema(description = "First name", example = "John")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;
    
    @Schema(description = "Profile image URL", example = "https://example.com/profile.jpg")
    @Size(max = 256, message = "Image URL must be less than 256 characters")
    private String image;
    
    @Schema(description = "Phone number", example = "+1-555-123-4567")
    @Size(max = 100, message = "Phone number must be less than 100 characters")
    private String phone;
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @Schema(description = "User type (INDIVIDUAL or COMPANY)", example = "INDIVIDUAL")
    private UserType userType;
    
    // Company specific fields
    @Schema(description = "Company name", example = "Acme Corporation")
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
    
    @Schema(description = "User status (0 for active, -1 for inactive)", example = "0", allowableValues = {"0", "-1"})
    private String status;
}