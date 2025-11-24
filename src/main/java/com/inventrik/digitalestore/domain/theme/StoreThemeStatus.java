package com.inventrik.digitalestore.domain.theme;

public enum StoreThemeStatus {
    ACTIVE("A"),
    INACTIVE("I");

    private final String code;

    StoreThemeStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static StoreThemeStatus fromCode(String code) {
        for (StoreThemeStatus status : StoreThemeStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown store theme status code: " + code);
    }
}
