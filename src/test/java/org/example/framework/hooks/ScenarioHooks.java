package org.example.framework.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import com.company.framework.ui.driver.DriverFactory;
import com.company.framework.ui.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ScenarioHooks {
    @Before("@ui")
    public void startBrowser() {
        DriverManager.set(DriverFactory.create());
        DriverManager.get().manage().window().maximize();
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
