package com.inventrik.digitalestore.domain.discount;

public enum DiscountType {
    PERCENTAGE("PERCENTAGE"),
    FIXED("FIXED");
    
    private final String value;
    
    DiscountType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static DiscountType fromValue(String value) {
        for (DiscountType type : DiscountType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown discount type: " + value);
    }
} 