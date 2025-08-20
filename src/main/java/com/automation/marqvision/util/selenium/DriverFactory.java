package com.automation.marqvision.util.selenium;
import com.automation.marqvision.util.IniConfig; // 패키지 경로는 프로젝트에 맞게
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public final class DriverFactory {
    private DriverFactory() {}

    /** 기본: 헤드리스 여부만 전달 */
    public static ChromeSession startChrome(Path downloadDir, boolean headless) {
        return startChrome(downloadDir, headless, null, null, null, null);
    }

    /** UiSettings 사용 (위치/크기/헤드리스) */
    public static ChromeSession startChrome(Path downloadDir, IniConfig.UiSettings ui) {
        return startChrome(downloadDir, ui.headless, ui.x, ui.y, ui.width, ui.height);
    }

    /** 상세 지정 버전 */
    public static ChromeSession startChrome(Path downloadDir,
                                            boolean headless,
                                            Integer x, Integer y,
                                            Integer width, Integer height) {
        ensureDir(downloadDir);

        // 고유 프로필 디렉터리 (표적 kill/세션 격리)
        Path profileDir = createTempProfileDir();

        ChromeOptions opts = buildOptions(downloadDir, headless, x, y, width, height, profileDir);
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingAnyFreePort()
                .withSilent(true)
                .build();

        try {
            service.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start ChromeDriverService", e);
        }

        WebDriver driver = new ChromeDriver(service, opts);

        // 일부 환경에서 인자 무시 → 보정
        if (!headless) {
            if (width != null && height != null && width > 0 && height > 0) {
                driver.manage().window().setSize(new Dimension(width, height));
            }
            if (x != null && y != null) {
                driver.manage().window().setPosition(new Point(x, y));
            }
        }
        return new ChromeSession(service, driver, profileDir);
    }

    // ────────── 내부 유틸 ──────────

    private static void ensureDir(Path dir) {
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
    }

    private static Path createTempProfileDir() {
        try {
            Path dir = Files.createTempDirectory("marqvision-profile-");
            dir.toFile().deleteOnExit();
            return dir;
        } catch (IOException e) {
            return null; // 프로필 없이도 동작하게
        }
    }

    private static ChromeOptions buildOptions(Path downloadDir,
                                              boolean headless,
                                              Integer x, Integer y,
                                              Integer width, Integer height,
                                              Path profileDir) {
        ChromeOptions opts = new ChromeOptions();

        // 다운로드 설정
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir.toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        prefs.put("plugins.always_open_pdf_externally", true);
        opts.setExperimentalOption("prefs", prefs);

        // 프로필 지정 (표적 종료/세션 격리)
        if (profileDir != null) {
            opts.addArguments("--user-data-dir=" + profileDir.toAbsolutePath());
        }

        boolean hasSize = width != null && height != null && width > 0 && height > 0;
        boolean hasPos  = x != null && y != null;

        if (headless) {
            opts.addArguments("--headless=new");
            if (hasSize) {
                opts.addArguments("--window-size=" + width + "," + height);
            }
        } else {
            if (hasSize) {
                opts.addArguments("--window-size=" + width + "," + height);
            } else {
                opts.addArguments("--start-maximized");
            }
            if (hasPos) {
                opts.addArguments("--window-position=" + x + "," + y);
            }
            // 종료 방해 옵션 금지
            opts.setExperimentalOption("detach", false);
        }

        return opts;
    }
}