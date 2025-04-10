package com.inventrik.digitalestore.domain.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "image", length = 256)  // Removed unique constraint
    private String image;
    
    @Column(name = "phone", unique = true, length = 100)
    private String phone;
    
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.INDIVIDUAL;
    
    @Column(name = "company_name", length = 100)
    private String companyName;
    
    @Column(name = "company_registration_number", length = 50)
    private String companyRegistrationNumber;
    
    @Column(name = "company_address1", length = 255)
    private String companyAddress1;
    
    @Column(name = "company_address2", length = 255)
    private String companyAddress2;
    
    @Column(name = "company_country", length = 255)
    private String companyCountry;
    
    @Column(name = "company_pincode", length = 255)
    private String companyPincode;
    
    @Column(name = "tax_id", length = 50)
    private String taxId;
    
    @Column(name = "otp", unique = true, length = 8)
    private String otp;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status;
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 2)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}