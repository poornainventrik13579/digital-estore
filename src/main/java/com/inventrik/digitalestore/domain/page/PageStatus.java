package com.inventrik.digitalestore.domain.page;

public enum PageStatus {
    DRAFT("draft"),
    PUBLISHED("published"),
    ARCHIVED("archived");
    
    private final String value;
    
    PageStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static PageStatus fromValue(String value) {
        for (PageStatus status : PageStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown page status: " + value);
    }
}
