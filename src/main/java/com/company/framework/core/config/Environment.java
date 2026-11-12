package com.company.framework.core.config;

public enum Environment {
    DEV, STAGING, CI;

    public static Environment from(String value) {
        return value == null || value.isBlank()
                ? DEV
                : valueOf(value.trim().toUpperCase());
    }
}
