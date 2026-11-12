package com.company.framework.core.listeners;

import com.company.framework.ui.driver.DriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

public final class TestListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        try {
            attachScreenshot();
        } catch (IllegalStateException ignored) {
            System.err.println("No browser session available for failure screenshot");
        }
    }

    @Attachment(value = "Failure screenshot", type = "image/png")
    private byte[] attachScreenshot() {
        return ((TakesScreenshot) DriverManager.get()).getScreenshotAs(OutputType.BYTES);
    }
}
