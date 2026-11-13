package com.company.framework.ui.driver;

import com.company.framework.core.config.ConfigManager;
import com.company.framework.mobile.config.Platform;
import com.company.framework.mobile.driver.MobileDriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;

public final class DriverFactory {
    private DriverFactory() {}

    public static WebDriver create() {
        String configuredPlatform = ConfigManager.optional("execution.platform");
        Platform platform = configuredPlatform == null || configuredPlatform.isBlank()
                ? Platform.WEB
                : Platform.from(configuredPlatform);
        return create(platform);
    }

    public static WebDriver create(Platform platform) {
        if (platform != Platform.WEB) {
            return MobileDriverFactory.create(platform);
        }
        String browser = ConfigManager.get("browser").toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigManager.get("headless"));
        String remote = remoteUrl();
        try {
            if (remote != null && !remote.isBlank()) {
                return new RemoteWebDriver(URI.create(remote).toURL(), options(browser, headless));
            }
            return switch (browser) {
                case "chrome" -> new ChromeDriver(chromeOptions(headless));
                case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
                default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
            };
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid remote.url", exception);
        }
    }

    private static String remoteUrl() {
        String configured = ConfigManager.optional("remote.url");
        String environment = System.getenv("SELENIUM_REMOTE_URL");
        return environment == null || environment.isBlank() ? configured : environment;
    }

    private static org.openqa.selenium.MutableCapabilities options(String browser, boolean headless) {
        return browser.equals("firefox") ? firefoxOptions(headless) : chromeOptions(headless);
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) options.addArguments("--headless=new");
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) options.addArguments("-headless");
        return options;
    }
}
