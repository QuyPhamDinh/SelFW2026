package com.company.framework.mobile.config;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public record DeviceProfile(
        String name,
        String platformName,
        String automationName,
        String deviceName,
        String platformVersion,
        String udid,
        String avd,
        String app,
        String appPackage,
        String appActivity,
        String bundleId,
        boolean noReset,
        boolean fullReset,
        boolean autoGrantPermissions,
        Map<String, JsonNode> additionalCapabilities) {

    public DeviceProfile {
        additionalCapabilities = Map.copyOf(additionalCapabilities);
    }

    public Platform platform() {
        return Platform.from(platformName);
    }

    public String applicationIdentifier() {
        return platform() == Platform.ANDROID ? appPackage : bundleId;
    }
}
