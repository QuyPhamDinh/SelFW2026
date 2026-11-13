package com.company.framework.mobile.config;

import java.util.Locale;
import java.util.Collection;

public enum Platform {
    WEB,
    ANDROID,
    IOS;

    public static Platform from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported execution platform: " + value
                    + ". Expected WEB, ANDROID, or IOS", exception);
        }
    }

    public static Platform fromTags(Collection<String> tags) {
        if (tags.contains("@android")) return ANDROID;
        if (tags.contains("@ios")) return IOS;
        return from(System.getProperty("mobile.platform",
                System.getenv().getOrDefault("MOBILE_PLATFORM", "ANDROID")));
    }
}
