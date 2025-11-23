package com.inventrik.digitalestore.domain.tax;

public enum TaxStatus {
    ACTIVE("A"),
    INACTIVE("I");

    private final String value;

    TaxStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaxStatus fromValue(String value) {
        for (TaxStatus status : TaxStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown tax status: " + value);
    }
}
