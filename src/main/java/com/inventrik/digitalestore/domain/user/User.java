package com.inventrik.digitalestore.domain.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
@IdClass(User.UserPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "image", length = 256)
    private String image;
    
    @Column(name = "phone", unique = true, length = 100)
    private String phone;
    
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.INDIVIDUAL;
    
    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;
    
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
    
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 50)
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
    
    /**
     * Check if user belongs to the specified tenant
     */
    public boolean belongsToTenant(Integer tenantId) {
        return this.tenantId != null && this.tenantId.equals(tenantId);
    }
    
    /**
     * Check if user is a tenant admin for their tenant
     */
    public boolean isTenantAdmin() {
        return UserRole.TENANT_ADMIN.equals(this.userRole);
    }
    
    /**
     * Check if user is a system admin
     */
    public boolean isSystemAdmin() {
        return UserRole.SYSTEM_ADMIN.equals(this.userRole);
    }
    
    /**
     * Check if user has admin privileges (either tenant admin or system admin)
     */
    public boolean hasAdminPrivileges() {
        return isSystemAdmin() || isTenantAdmin();
    }
    
    /**
     * Get full name (first + last)
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return String.format("%s %s", 
            firstName != null ? firstName : "", 
            lastName != null ? lastName : "").trim();
    }
    
    public static class UserPK implements Serializable {
        private Integer tenantId;
        private Long userId;
        
        public UserPK() {}
        
        public UserPK(Integer tenantId, Long userId) {
            this.tenantId = tenantId;
            this.userId = userId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserPK)) return false;
            UserPK userPK = (UserPK) o;
            return Objects.equals(tenantId, userPK.tenantId) && Objects.equals(userId, userPK.userId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, userId);
        }
    }
}