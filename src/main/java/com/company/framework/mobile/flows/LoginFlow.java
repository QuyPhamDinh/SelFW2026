package com.company.framework.mobile.flows;

import com.company.framework.mobile.screens.LoginScreen;

public final class LoginFlow {
    private final LoginScreen screen;

    public LoginFlow(LoginScreen screen) {
        this.screen = screen;
    }

    public boolean login(String username, String password) {
        screen.enterUsername(username);
        screen.enterPassword(password);
        screen.tapLogin();
        return screen.isHomeScreenDisplayed();
    }
}
