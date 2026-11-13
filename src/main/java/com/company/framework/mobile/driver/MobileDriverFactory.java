package com.company.framework.mobile.driver;

import com.company.framework.mobile.config.DeviceProfile;
import com.company.framework.mobile.config.MobileConfig;
import com.company.framework.mobile.config.Platform;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.util.Map;

public final class MobileDriverFactory {
    private MobileDriverFactory() {}

    public static WebDriver create(Platform platform) {
        DeviceProfile profile = MobileConfig.selectedProfile();
        if (profile.platform() != platform) {
            throw new IllegalArgumentException("Selected profile '" + profile.name()
                    + "' targets " + profile.platform() + ", not " + platform);
        }
        try {
            DevicePrerequisites.validate(profile, MobileConfig.serverUrl());
            return platform == Platform.ANDROID
                    ? new AndroidDriver(MobileConfig.serverUrl().toURL(),
                    (UiAutomator2Options) optionsFor(platform, profile))
                    : new IOSDriver(MobileConfig.serverUrl().toURL(),
                    (XCUITestOptions) optionsFor(platform, profile));
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid Appium server URL: "
                    + MobileConfig.serverUrl(), exception);
        }
    }

    public static MutableCapabilities optionsFor(Platform platform, DeviceProfile profile) {
        if (profile.platform() != platform) {
            throw new IllegalArgumentException("Profile platform does not match requested platform");
        }
        MutableCapabilities options = platform == Platform.ANDROID
                ? new UiAutomator2Options()
                : new XCUITestOptions();
        options.setCapability("platformName", profile.platformName());
        options.setCapability("appium:automationName", profile.automationName());
        options.setCapability("appium:deviceName", profile.deviceName());
        set(options, "appium:platformVersion", profile.platformVersion());
        set(options, "appium:udid", profile.udid());
        set(options, "appium:avd", profile.avd());
        set(options, "appium:app", profile.app());
        set(options, "appium:appPackage", profile.appPackage());
        set(options, "appium:appActivity", profile.appActivity());
        set(options, "appium:bundleId", profile.bundleId());
        options.setCapability("appium:noReset", profile.noReset());
        options.setCapability("appium:fullReset", profile.fullReset());
        if (platform == Platform.ANDROID) {
            options.setCapability("appium:autoGrantPermissions", profile.autoGrantPermissions());
        }
        for (Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> capability
                : profile.additionalCapabilities().entrySet()) {
            if (capability.getKey().startsWith("appium:")) {
                options.setCapability(capability.getKey(), capability.getValue().isNumber()
                        ? capability.getValue().numberValue()
                        : capability.getValue().isBoolean()
                        ? capability.getValue().booleanValue()
                        : capability.getValue().asText());
            }
        }
        return options;
    }

    private static void set(MutableCapabilities options, String key, String value) {
        if (value != null && !value.isBlank()) options.setCapability(key, value);
    }
}
