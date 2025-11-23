package com.inventrik.digitalestore.domain.tenant;

public enum TenantStatus {
    ACTIVE("A"),
    INACTIVE("I");

    private final String code;

    TenantStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static TenantStatus fromCode(String code) {
        for (TenantStatus status : TenantStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown tenant status code: " + code);
    }
}
