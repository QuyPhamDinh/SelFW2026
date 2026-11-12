package org.example.framework.web;

import com.company.framework.core.config.ConfigManager;
import com.company.framework.ui.driver.DriverManager;
import org.openqa.selenium.By;

public final class WebFormPage extends BasePage {
    private final By message = By.name("my-text");
    private final By submit = By.cssSelector("button");
    private final By result = By.id("message");

    public void open() {
        DriverManager.get().get(ConfigManager.get("ui.base.url"));
    }

    public void submitMessage(String value) {
        type(message, value);
        click(submit);
    }

    public String result() {
        return text(result);
    }
}
