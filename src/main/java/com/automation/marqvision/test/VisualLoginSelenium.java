package com.automation.marqvision.test;

import com.automation.marqvision.util.EnvUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class VisualLoginSelenium {
    public static void main(String[] args) {
        final String EMAIL = EnvUtils.requireEnv("MARQVISION_EMAIL");
        final String PASSWORD = EnvUtils.requireEnv("MARQVISION_PASSWORD");

        ChromeOptions opts = new ChromeOptions();
        // 헤드리스 끄기 → 눈으로 보임
        // opts.addArguments("--headless=new"); // 필요시 주석 해제
        opts.addArguments("--start-maximized");
        WebDriver driver = new ChromeDriver(opts);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            driver.get("https://brand.marqvision.com/login");

            // 폼 대기 (헤더 텍스트가 있다면 대기, 없으면 입력창으로 대기)
            // wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign into your account')]")));

            // 이메일 입력 (placeholder/속성 기반: 좌표X)
            WebElement email = firstPresent(driver,
                    By.cssSelector("input[type='email']"),
                    By.cssSelector("input[name='email']"),
                    By.cssSelector("input[placeholder*='Email' i]")
            );
            wait.until(ExpectedConditions.visibilityOf(email));
            highlight(driver, email);
            email.clear(); email.sendKeys(EMAIL);

            // 비밀번호 입력
            WebElement pwd = firstPresent(driver,
                    By.cssSelector("input[type='password']"),
                    By.cssSelector("input[name='password']"),
                    By.cssSelector("input[placeholder*='Password' i]")
            );
            wait.until(ExpectedConditions.visibilityOf(pwd));
            highlight(driver, pwd);
            pwd.clear(); pwd.sendKeys(PASSWORD);

            // 버튼 찾기 (여러 후보를 순서대로 시도)
            WebElement btn = firstPresent(driver,
                    By.xpath("//button[@type='submit' and .//p[normalize-space()='Sign in']]"),
                    By.xpath("//button[@type='submit' and contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]"),
                    By.xpath("//button[.//p[normalize-space()='Sign in']]")
            );

            // 보이도록 하이라이트 + 클릭 가능 상태 대기 후 클릭
            highlight(driver, btn);
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.elementToBeClickable(btn));

            try {
                btn.click();
            } catch (ElementClickInterceptedException e) {
                // 오버레이가 가로막을 때 JS 클릭 백업
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }

            // 로그인 성공 판정 (URL 변화 또는 대시보드 마커 대기)
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> !d.getCurrentUrl().contains("/login"));
            System.out.println("OK: " + driver.getCurrentUrl());

        } finally {
            // driver.quit(); // 눈으로 확인하고 싶으면 지연 후 종료
        }
    }

    // 다중 후보 중 먼저 있는 요소 반환
    static WebElement firstPresent(WebDriver d, By... candidates) {
        for (By by : candidates) {
            var els = d.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("No candidate matched");
    }

    // 클릭 직전 요소에 하이라이트(시각 확인용)
    static void highlight(WebDriver d, WebElement el) {
        ((JavascriptExecutor)d).executeScript(
                "arguments[0].style.outline='3px solid magenta'; arguments[0].scrollIntoView({block:'center'});", el);
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
    }
}
