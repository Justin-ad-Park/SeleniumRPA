package com.automation.marqvision.usecase;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.automation.marqvision.util.selenium.SeleniumUtils.highlight;

public class MonitoringNavigator {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public MonitoringNavigator(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    public void goToMonitoring() {
        driver.get("https://brand.marqvision.com/price-monitoring/monitoring");

        // Export Chip (role=button + 텍스트 Export)
        By exportChip = By.xpath("//div[@role='button' and .//span[normalize-space()='Export']]");
        WebElement chip = wait.until(ExpectedConditions.visibilityOfElementLocated(exportChip));
        highlight(driver, chip);
    }
}
