package com.inventrik.digitalestore.domain.user;

/**
 * Enum defining user roles in the system
 */
public enum UserRole {
    USER("Regular User"),
    ADMIN("Administrator"),
    MANAGER("Manager");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 