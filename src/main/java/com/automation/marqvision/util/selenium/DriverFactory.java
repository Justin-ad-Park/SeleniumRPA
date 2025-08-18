package com.automation.marqvision.util.selenium;

import com.automation.marqvision.util.IniConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {

    /** 기본: 헤드리스 여부만 전달 (비헤드리스면 최대화) */
    public static WebDriver createChrome(Path downloadDir, boolean headless) {
        ensureDir(downloadDir);
        ChromeOptions opts = buildOptions(downloadDir, headless, null, null, null, null, true);
        return new ChromeDriver(opts);
    }

    /** 위치/크기/헤드리스까지 모두 지정 (UiSettings) */
    public static WebDriver createChrome(Path downloadDir, IniConfig.UiSettings ui) {
        ensureDir(downloadDir);
        ChromeOptions opts = buildOptions(downloadDir, ui.headless, ui.x, ui.y, ui.width, ui.height, false);
        WebDriver driver = new ChromeDriver(opts);

        // 일부 환경에서 CLI 인자가 무시될 수 있어 최종 보정
        if (!ui.headless) {
            if (ui.width > 0 && ui.height > 0) {
                driver.manage().window().setSize(new Dimension(ui.width, ui.height));
            }
            driver.manage().window().setPosition(new Point(ui.x, ui.y));
        }
        return driver;
    }

    // ─────────────── private helpers ───────────────

    private static void ensureDir(Path dir) {
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
    }

    /**
     * 공통 옵션 빌더
     * @param maximizeWhenNoSize  non-headless에서 width/height 미지정 시 --start-maximized 적용 여부
     */
    private static ChromeOptions buildOptions(Path downloadDir,
                                              boolean headless,
                                              Integer x, Integer y,
                                              Integer width, Integer height,
                                              boolean maximizeWhenNoSize) {
        ChromeOptions opts = new ChromeOptions();

        // 다운로드 설정 (중복 제거)
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir.toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        // PDF 뷰어 대신 바로 저장하고 싶을 때 도움
        prefs.put("plugins.always_open_pdf_externally", true);
        opts.setExperimentalOption("prefs", prefs);

        boolean hasSize = width != null && height != null && width > 0 && height > 0;
        boolean hasPos  = x != null && y != null;

        if (headless) {
            // headless는 위치 개념 없음 → viewport만 지정
            opts.addArguments("--headless=new");
            if (hasSize) {
                opts.addArguments("--window-size=" + width + "," + height);
            }
        } else {
            if (hasSize) {
                opts.addArguments("--window-size=" + width + "," + height);
            } else if (maximizeWhenNoSize) {
                // 사이즈 미지정이면 전체화면로 시작(기존 동작 유지)
                opts.addArguments("--start-maximized");
            }
            if (hasPos) {
                opts.addArguments("--window-position=" + x + "," + y);
            }
        }
        return opts;
    }
}