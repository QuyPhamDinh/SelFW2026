package com.company.framework.ui.pages;

import com.company.framework.core.utils.WaitUtils;
import com.company.framework.ui.driver.DriverManager;
import org.openqa.selenium.By;

public abstract class BasePage {
    protected void click(By locator) {
        WaitUtils.explicit(DriverManager.get()).until(driver -> driver.findElement(locator)).click();
    }

    protected void type(By locator, String value) {
        var element = WaitUtils.explicit(DriverManager.get()).until(driver -> driver.findElement(locator));
        element.clear();
        element.sendKeys(value);
    }
}
