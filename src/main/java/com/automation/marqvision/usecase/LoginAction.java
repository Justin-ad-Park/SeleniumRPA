package com.automation.marqvision.usecase;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import static com.automation.marqvision.util.selenium.SeleniumUtils.firstPresent;
import static com.automation.marqvision.util.selenium.SeleniumUtils.highlight;

public class LoginAction {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginAction(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    public void login(String emailValue, String passwordValue) {
        driver.get("https://brand.marqvision.com/login");

        WebElement email = firstPresent(driver,
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[name='email']"),
                By.cssSelector("input[placeholder*='Email' i]")
        );
        wait.until(ExpectedConditions.visibilityOf(email));
        highlight(driver, email);
        email.clear(); email.sendKeys(emailValue);

        WebElement pwd = firstPresent(driver,
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[name='password']"),
                By.cssSelector("input[placeholder*='Password' i]")
        );
        wait.until(ExpectedConditions.visibilityOf(pwd));
        highlight(driver, pwd);
        pwd.clear(); pwd.sendKeys(passwordValue);

        // 로그인 버튼 (자식 <p> 안의 텍스트 'Sign in')
        WebElement btn = firstPresent(driver,
                By.xpath("//button[@type='submit' and .//p[normalize-space()='Sign in']]"),
                By.xpath("//button[@type='submit' and contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]"),
                By.xpath("//button[.//p[normalize-space()='Sign in']]"),
                By.cssSelector("button[type='submit']")
        );
        highlight(driver, btn);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        // /login 벗어날 때까지 대기
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> !d.getCurrentUrl().contains("/login"));
    }
}
