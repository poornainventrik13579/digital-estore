package com.inventrik.digitalestore.domain.user;

/**
 * Enum defining user roles in the Inventrik multi-tenant system
 * Hierarchy: SYSTEM_ADMIN > TENANT_ADMIN > USER
 */
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
    
    /**
     * Check if this role has higher or equal privileges than the given role
     */
    public boolean hasPrivilegeOf(UserRole role) {
        return this.ordinal() <= role.ordinal();
    }
    
    /**
     * Check if this is a tenant-level role (not system admin)
     */
    public boolean isTenantRole() {
        return this != SYSTEM_ADMIN;
    }
} 