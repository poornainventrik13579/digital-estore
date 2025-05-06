package com.inventrik.digitalestore.domain.payment;

public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SUCCESSFUL("Successful"),
    FAILED("Failed"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded");
    
    private final String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static PaymentStatus fromDisplayName(String displayName) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getDisplayName().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }
}