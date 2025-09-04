package com.inventrik.digitalestore.dto.request;

import com.inventrik.digitalestore.domain.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantUserUpdateRequest {
    
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;
    
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;
    
    @Email(message = "Valid email address is required")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @Size(max = 100, message = "Phone number must be less than 100 characters")
    private String phone;
    
    @Size(max = 256, message = "Image URL must be less than 256 characters")
    private String image;
    
    private UserType userType;
    
    // Company details (optional for BUSINESS users)
    @Size(max = 100, message = "Company name must be less than 100 characters")
    private String companyName;
    
    @Size(max = 50, message = "Company registration number must be less than 50 characters")
    private String companyRegistrationNumber;
    
    @Size(max = 255, message = "Company address must be less than 255 characters")
    private String companyAddress1;
    
    @Size(max = 255, message = "Company address must be less than 255 characters")
    private String companyAddress2;
    
    @Size(max = 255, message = "Company country must be less than 255 characters")
    private String companyCountry;
    
    @Size(max = 20, message = "Postal code must be less than 20 characters")
    private String companyPincode;
    
    @Size(max = 50, message = "Tax ID must be less than 50 characters")
    private String taxId;
}
