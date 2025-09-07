package com.inventrik.digitalestore.dto.request;

import com.inventrik.digitalestore.domain.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantUserSignupRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email address is required")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password;
    
    @Size(max = 100, message = "Phone number must be less than 100 characters")
    private String phone;
    
    @Size(max = 256, message = "Image URL must be less than 256 characters")
    private String image;
    
    private UserType userType = UserType.INDIVIDUAL;
    
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
    
    @Size(max = 255, message = "Company pincode must be less than 255 characters")
    private String companyPincode;
    
    @Size(max = 50, message = "Tax ID must be less than 50 characters")
    private String taxId;
}
