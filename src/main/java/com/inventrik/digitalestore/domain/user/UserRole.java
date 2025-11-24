package com.inventrik.digitalestore.domain.user;

public enum UserRole {
    SYSTEM_ADMIN("System Administrator", "Full system access across all tenants"),
    TENANT_ADMIN("Tenant Administrator", "Full access within their tenant store"),
    USER("Regular User", "Limited access within their tenant store");
    
    private final String displayName;
    private final String description;
    
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean hasPrivilegeOf(UserRole role) {
        return this.ordinal() <= role.ordinal();
    }
    
    public boolean isTenantRole() {
        return this != SYSTEM_ADMIN;
    }
} 