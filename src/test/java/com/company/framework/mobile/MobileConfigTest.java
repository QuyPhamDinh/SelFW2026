package com.company.framework.mobile;

import com.company.framework.mobile.config.DeviceProfile;
import com.company.framework.mobile.config.MobileConfig;
import com.company.framework.mobile.config.Platform;
import com.company.framework.mobile.driver.MobileDriverFactory;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;

public class MobileConfigTest {
    @Test
    public void loadsValidProfileAndPreservesBoolean() {
        DeviceProfile profile = MobileConfig.loadProfile(json(
                "\"android\": {\"platformName\":\"Android\",\"automationName\":\"UiAutomator2\","
                        + "\"deviceName\":\"emulator\",\"noReset\":false,\"fullReset\":true}"), "android");

        assertEquals(profile.platform(), Platform.ANDROID);
        assertFalse(profile.noReset());
        assertEquals(profile.fullReset(), true);
    }

    @Test
    public void rejectsMissingProfile() {
        assertThrows(IllegalArgumentException.class,
                () -> MobileConfig.loadProfile(json("\"android\": {}"), "missing"));
    }

    @Test
    public void rejectsMalformedJson() {
        assertThrows(IllegalArgumentException.class,
                () -> MobileConfig.loadProfile(stream("{"), "android"));
    }

    @Test
    public void rejectsMissingRequiredFields() {
        assertThrows(IllegalArgumentException.class,
                () -> MobileConfig.loadProfile(json("\"android\":{\"platformName\":\"Android\"}"), "android"));
    }

    @Test
    public void buildsIndependentCapabilityObjects() {
        DeviceProfile first = MobileConfig.loadProfile(json(
                "\"android\":{\"platformName\":\"Android\",\"automationName\":\"UiAutomator2\","
                        + "\"deviceName\":\"one\",\"noReset\":true}"), "android");
        DeviceProfile second = MobileConfig.loadProfile(json(
                "\"android\":{\"platformName\":\"Android\",\"automationName\":\"UiAutomator2\","
                        + "\"deviceName\":\"two\",\"noReset\":false}"), "android");

        assertEquals(MobileDriverFactory.optionsFor(Platform.ANDROID, first)
                .getCapability("appium:deviceName"), "one");
        assertEquals(MobileDriverFactory.optionsFor(Platform.ANDROID, second)
                .getCapability("appium:deviceName"), "two");
    }

    private static ByteArrayInputStream json(String profile) {
        return stream("{\"devices\":{" + profile + "}}");
    }

    private static ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
