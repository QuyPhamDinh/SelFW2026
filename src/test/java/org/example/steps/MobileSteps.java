package org.example.steps;

import com.company.framework.mobile.config.Platform;
import com.company.framework.ui.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.cucumber.java.en.Then;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MobileSteps {
    @Then("the Android Appium session should be available")
    public void verifyAndroidSession() {
        assertTrue(DriverManager.get() instanceof AppiumDriver, "Expected an Appium driver");
        AppiumDriver driver = (AppiumDriver) DriverManager.get();
        assertEquals(driver.getCapabilities().getPlatformName().toString().toUpperCase(),
                Platform.ANDROID.name());
    }
}
