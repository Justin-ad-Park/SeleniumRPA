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
                By.cssSelector("input[placeholder*='Email' i]") //input 태그를 대상으로 placeholder에 Email 문구가 포함된 것. i는 대소문자 구분 안함
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
                /*
                //button[@type='submit' and .//p[normalize-space()='Sign in']]
                    button[@type='submit'] → type="submit" 속성을 가진 <button> 찾기
                    and .//p[...] → 그 버튼 안에 <p> 태그가 있고 normalize-space()='Sign in' → 공백을 정리(normalize-space)한 텍스트가 정확히 "Sign in"인 경우만 매칭

                 //By.xpath("//button[@type='submit' and contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]")
                    @type='submit' → submit 버튼
                    normalize-space(.) → 버튼 전체의 텍스트를 공백 정리
                    translate(...,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz') → 영문 대문자를 소문자로 바꿔서 비교 (즉, 대소문자 무시)
                    contains(...,'sign in') → "sign in"이 포함되어 있는지 검사

                //By.xpath("//button[.//p[normalize-space()='Sign in']]")
                    버튼 안에 <p>가 있어야 함. normalize-space(.) → 버튼 전체의 텍스트를 공백 정리. 그 텍스트가 "Sign in"이면 매칭
                 */
                By.xpath("//button[@type='submit' and .//p[normalize-space()='Sign in']]"),
                By.xpath("//button[@type='submit' and contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]"),
                By.xpath("//button[.//p[normalize-space()='Sign in']]"),
                By.cssSelector("button[type='submit']")
        );
        highlight(driver, btn);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();   //엘리먼트가 클릭 가능한 상태까지 대기 후 클릭
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);  // 직접 클릭 스크립트 실행
        }

        // /login 벗어날 때까지 대기
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> !d.getCurrentUrl().contains("/login"));
    }
}
