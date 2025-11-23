package com.inventrik.digitalestore.domain.tax;

public enum TaxDefaultFlag {
    YES("Y"),
    NO("N");

    private final String value;

    TaxDefaultFlag(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaxDefaultFlag fromValue(String value) {
        for (TaxDefaultFlag flag : TaxDefaultFlag.values()) {
            if (flag.value.equals(value)) {
                return flag;
            }
        }
        throw new IllegalArgumentException("Unknown tax default flag: " + value);
    }
}
