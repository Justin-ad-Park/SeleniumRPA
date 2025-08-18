package com.automation.marqvision.util.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {
    public static WebDriver createChrome(Path downloadDir, boolean headless) {
        try {
            Files.createDirectories(downloadDir);
        } catch (Exception ignored) {}

        ChromeOptions opts = new ChromeOptions();
        if (headless) opts.addArguments("--headless=new");
        opts.addArguments("--start-maximized");

        // 다운로드 설정
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir.toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        // PDF/안전경고 회피(필요 시)
        prefs.put("plugins.always_open_pdf_externally", true);
        opts.setExperimentalOption("prefs", prefs);

        return new ChromeDriver(opts);
    }

    // 브라우저의 위치와 창크기를 지정하는 오버로드 추가
    public static WebDriver createChrome(Path downloadDir,
                                         boolean headless,
                                         Integer x, Integer y,
                                         Integer width, Integer height) {
        try { Files.createDirectories(downloadDir); } catch (Exception ignored) {}

        ChromeOptions opts = new ChromeOptions();

        // 다운로드 폴더 지정
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir.toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        opts.setExperimentalOption("prefs", prefs);

        // ✅ 위치/크기 지정 (헤드리스가 아니어야 실제 창에 적용됨)
        if (headless) {
            // headless는 위치 개념 없음. viewport만 조정 가능
            opts.addArguments("--headless=new");
            if (width != null && height != null) {
                opts.addArguments("--window-size=" + width + "," + height);
            } else {
                opts.addArguments("--window-size=1280,900");
            }
        } else {
            // start-maximized는 위치/크기 지정과 충돌하므로 사용하지 않음
            if (width != null && height != null) {
                opts.addArguments("--window-size=" + width + "," + height);
            }
            if (x != null && y != null) {
                opts.addArguments("--window-position=" + x + "," + y);
            }
        }

        WebDriver driver = new ChromeDriver(opts);

        // 🔧 일부 OS/드라이버 조합에서 인자만으로 안 먹는 경우가 있어, 한 번 더 보정
        if (!headless) {
            if (width != null && height != null) {
                driver.manage().window().setSize(new org.openqa.selenium.Dimension(width, height));
            }
            if (x != null && y != null) {
                driver.manage().window().setPosition(new org.openqa.selenium.Point(x, y));
            }
        }

        return driver;
    }
}
