package com.automation.marqvision.util.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.nio.file.Path;

public final class ChromeSession implements AutoCloseable {
    private final ChromeDriverService service;
    private final WebDriver driver;
    private final Path profileDir; // --user-data-dir

    ChromeSession(ChromeDriverService service, WebDriver driver, Path profileDir) {
        this.service = service;
        this.driver = driver;
        this.profileDir = profileDir;

        // 비정상 종료 대비
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    public WebDriver driver() { return driver; }

    @Override public void close() { cleanup(); }

    private void cleanup() {
        try { driver.quit(); } catch (Throwable ignored) {}
        try { service.stop(); } catch (Throwable ignored) {}
        if (profileDir != null) {
            // 혹시라도 남아있으면 해당 프로필로 띄운 크롬/드라이버만 표적으로 종료
            ChromeKiller.killByUserDataDir(profileDir);
        }
    }
}
