package com.company.framework.mobile.screens.android;

import com.company.framework.core.config.ConfigManager;
import com.company.framework.mobile.actions.MobileActions;
import com.company.framework.mobile.screens.LoginScreen;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public final class AndroidLoginScreen implements LoginScreen {
    private final MobileActions actions = new MobileActions();
    private final By username = AppiumBy.accessibilityId(required("android.login.username.accessibility.id"));
    private final By password = AppiumBy.accessibilityId(required("android.login.password.accessibility.id"));
    private final By login = AppiumBy.accessibilityId(required("android.login.button.accessibility.id"));
    private final By home = AppiumBy.accessibilityId(required("android.home.accessibility.id"));

    @Override
    public void enterUsername(String value) {
        actions.enterText(username, value);
    }

    @Override
    public void enterPassword(String value) {
        actions.enterText(password, value);
    }

    @Override
    public void tapLogin() {
        actions.tap(login);
    }

    @Override
    public boolean isHomeScreenDisplayed() {
        return actions.isDisplayed(home);
    }

    private static String required(String key) {
        String value = ConfigManager.optional(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing Android screen configuration: " + key);
        }
        return value;
    }
}
