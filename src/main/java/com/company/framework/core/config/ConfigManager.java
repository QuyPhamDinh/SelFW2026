package com.company.framework.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {
    private static final Properties PROPERTIES = load();

    private ConfigManager() {}

    public static String get(String key) {
        String value = override(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required configuration is missing: " + key);
        }
        return value;
    }

    public static String optional(String key) {
        return override(key);
    }

    public static int integer(String key) {
        return Integer.parseInt(get(key));
    }

    private static String override(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return value == null || value.isBlank() ? PROPERTIES.getProperty(key) : value;
    }

    private static Properties load() {
        String environment = System.getProperty("env", System.getenv("TEST_ENV"));
        Environment selected = Environment.from(environment);
        Properties properties = new Properties();
        try (InputStream base = ConfigManager.class.getClassLoader().getResourceAsStream("config/" + selected.name().toLowerCase() + ".properties")) {
            if (base == null) {
                throw new IllegalStateException("No configuration exists for environment: " + selected);
            }
            properties.load(base);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load environment configuration", exception);
        }
    }
}
