package com.inventrik.digitalestore.domain.page;

public enum PageVisibility {
    PUBLIC("public"),
    PRIVATE("private"),
    INTERNAL("internal");
    
    private final String value;
    
    PageVisibility(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static PageVisibility fromValue(String value) {
        for (PageVisibility visibility : PageVisibility.values()) {
            if (visibility.value.equals(value)) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Unknown page visibility: " + value);
    }
}
