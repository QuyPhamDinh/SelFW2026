package com.company.framework.core.utils;

import com.company.framework.core.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class WaitUtils {
    private WaitUtils() {}

    public static WebDriverWait explicit(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.integer("ui.wait.seconds")));
    }
}
