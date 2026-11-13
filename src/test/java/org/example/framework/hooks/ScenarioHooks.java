package org.example.framework.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import com.company.framework.ui.driver.DriverFactory;
import com.company.framework.ui.driver.DriverManager;
import com.company.framework.mobile.config.Platform;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

public class ScenarioHooks {
    @Before("@ui")
    public void startBrowser() {
        DriverManager.set(DriverFactory.create());
        DriverManager.get().manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(30))
                .scriptTimeout(Duration.ofSeconds(15));
        DriverManager.get().manage().window().maximize();
    }

    @Before("@mobile")
    public void startMobile(Scenario scenario) {
        Platform platform = Platform.fromTags(scenario.getSourceTagNames());
        var profile = com.company.framework.mobile.config.MobileConfig.selectedProfile();
        Allure.parameter("mobile.profile", profile.name());
        Allure.parameter("mobile.platform", profile.platformName());
        Allure.parameter("mobile.device", profile.deviceName());
        Allure.parameter("mobile.platform.version",
                profile.platformVersion() == null ? "unspecified" : profile.platformVersion());
        DriverManager.set(DriverFactory.create(platform));
        DriverManager.get().manage().timeouts()
                .implicitlyWait(Duration.ZERO);
    }

    @After(value = "@ui", order = 100)
    public void captureFailureAndStopBrowser(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                captureScreenshot(scenario);
            }
        } finally {
            DriverManager.quit();
        }
    }

    @After(value = "@mobile", order = 100)
    public void captureMobileFailureAndStopDriver(Scenario scenario) {
        try {
            if (scenario.isFailed() && DriverManager.hasDriver()) {
                captureScreenshot(scenario);
            }
        } finally {
            DriverManager.quit();
        }
    }

    private void captureScreenshot(Scenario scenario) {
        byte[] screenshot = ((TakesScreenshot) DriverManager.get()).getScreenshotAs(OutputType.BYTES);
        scenario.attach(screenshot, "image/png", "Failure screenshot");
        String fileName = scenario.getName().replaceAll("[^a-zA-Z0-9._-]+", "_")
                + "-" + UUID.randomUUID() + ".png";
        try {
            Path directory = Path.of("target", "screenshots");
            Files.createDirectories(directory);
            Files.write(directory.resolve(fileName), screenshot);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist failure screenshot", exception);
        }
    }
}
