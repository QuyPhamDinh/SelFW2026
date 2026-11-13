package com.company.framework.mobile.config;

import com.company.framework.core.config.ConfigManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public final class MobileConfig {
    private static final String DEVICES_RESOURCE = "config/mobile/devices.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MobileConfig() {}

    public static DeviceProfile selectedProfile() {
        String name = System.getProperty("mobile.device",
                System.getenv().getOrDefault("MOBILE_DEVICE", "android-emulator"));
        try (InputStream stream = MobileConfig.class.getClassLoader()
                .getResourceAsStream(DEVICES_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing classpath resource: " + DEVICES_RESOURCE);
            }
            return loadProfile(stream, name);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + DEVICES_RESOURCE, exception);
        }
    }

    public static DeviceProfile loadProfile(InputStream stream, String profileName) {
        try {
            JsonNode root = MAPPER.readTree(stream);
            JsonNode devices = root == null ? null : root.get("devices");
            JsonNode profile = devices == null ? null : devices.get(profileName);
            if (profile == null || !profile.isObject()) {
                throw new IllegalArgumentException("Mobile device profile '" + profileName
                        + "' was not found in " + DEVICES_RESOURCE);
            }
            return toProfile(profileName, profile);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Malformed mobile device JSON for profile '"
                    + profileName + "'", exception);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read mobile device JSON for profile '"
                    + profileName + "'", exception);
        }
    }

    public static URI serverUrl() {
        String value = ConfigManager.optional("appium.server.url");
        value = environmentOrDefault("APPIUM_SERVER_URL", value, "http://127.0.0.1:4723");
        try {
            URI uri = URI.create(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Appium URL must include scheme and host: " + value);
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid Appium server URL: " + value, exception);
        }
    }

    public static int waitSeconds() {
        String value = ConfigManager.optional("mobile.wait.seconds");
        return Integer.parseInt(value == null || value.isBlank() ? "15" : value);
    }

    private static DeviceProfile toProfile(String name, JsonNode node) {
        String platformName = required(node, "platformName", name);
        String automationName = required(node, "automationName", name);
        String deviceName = required(node, "deviceName", name);
        Platform platform = Platform.from(platformName);
        if (platform == Platform.WEB) {
            throw new IllegalArgumentException("Mobile profile '" + name + "' cannot use WEB");
        }
        String app = text(node, "app");
        String appPackage = text(node, "appPackage");
        String appActivity = text(node, "appActivity");
        String bundleId = text(node, "bundleId");
        Map<String, JsonNode> additional = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (!KNOWN_FIELDS.contains(field.getKey())) {
                additional.put(field.getKey(), field.getValue().deepCopy());
            }
        }
        return new DeviceProfile(name, platformName, automationName,
                environmentOrDefault("MOBILE_DEVICE_NAME", deviceName, deviceName),
                environmentOrDefault("MOBILE_PLATFORM_VERSION", text(node, "platformVersion"), null),
                override("udid", text(node, "udid")),
                text(node, "avd"),
                override("app", app),
                environmentOrDefault("MOBILE_APP_PACKAGE", appPackage, null),
                environmentOrDefault("MOBILE_APP_ACTIVITY", appActivity, null),
                environmentOrDefault("IOS_BUNDLE_ID", bundleId, null),
                booleanOverride("MOBILE_NO_RESET", node, "noReset", false),
                booleanOverride("MOBILE_FULL_RESET", node, "fullReset", false),
                booleanOverride("MOBILE_AUTO_GRANT_PERMISSIONS", node,
                        "autoGrantPermissions", false),
                additional);
    }

    private static final java.util.Set<String> KNOWN_FIELDS = java.util.Set.of(
            "platformName", "automationName", "deviceName", "platformVersion", "udid",
            "avd", "app", "appPackage", "appActivity", "bundleId", "noReset",
            "fullReset", "autoGrantPermissions");

    private static String required(JsonNode node, String key, String profile) {
        String value = text(node, key);
        if (isBlank(value)) {
            throw new IllegalArgumentException("Mobile profile '" + profile
                    + "' requires non-empty '" + key + "'");
        }
        return value;
    }

    private static String text(JsonNode node, String key) {
        JsonNode value = node.get(key);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static boolean booleanValue(JsonNode node, String key, boolean defaultValue) {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) return defaultValue;
        if (!value.isBoolean()) {
            throw new IllegalArgumentException("Mobile capability '" + key + "' must be boolean");
        }
        return value.booleanValue();
    }

    private static boolean booleanOverride(String environmentKey, JsonNode node,
                                           String key, boolean defaultValue) {
        String override = System.getenv(environmentKey);
        if (override == null || override.isBlank()) {
            return booleanValue(node, key, defaultValue);
        }
        if (!override.equalsIgnoreCase("true") && !override.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(environmentKey + " must be true or false");
        }
        return Boolean.parseBoolean(override);
    }

    private static String override(String key, String configured) {
        String environmentKey = "MOBILE_" + key.replace('.', '_').toUpperCase(Locale.ROOT);
        return environmentOrDefault(environmentKey, configured, null);
    }

    private static String environmentOrDefault(String key, String configured, String defaultValue) {
        String environment = System.getenv(key);
        return environment == null || environment.isBlank()
                ? (configured == null || configured.isBlank() ? defaultValue : configured)
                : environment;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
