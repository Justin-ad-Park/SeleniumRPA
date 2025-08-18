package com.automation.marqvision.workflow;

import com.automation.marqvision.util.EnvUtils;
import com.automation.marqvision.util.IniConfig;
import com.automation.marqvision.util.selenium.DriverFactory;
import com.automation.marqvision.usecase.*;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class MarqvisionWorkflowRunner {
    public static void main(String[] args) {
        final String EMAIL = EnvUtils.requireEnv("MARQVISION_EMAIL");
        final String PASSWORD = EnvUtils.requireEnv("MARQVISION_PASSWORD");

        Path downloadDir = Paths.get(System.getProperty("user.home"), "Downloads", "marqvision");

        // 0) 기존 파일 백업 (가장 첫 단계)
        int backedUp = DownloadBackupManager.backupToSubfolder(downloadDir);
        System.out.println("[OK] Backup moved files: " + backedUp);

        // 2) UI 설정값 로드 (config.ini → 기본값 자동 폴백)
        IniConfig.UiSettings ui = IniConfig.load();
        System.out.println("[UI] " + ui);

        WebDriver driver = DriverFactory.createChrome(downloadDir, ui);

        try {
            // 1) 로그인
            new LoginAction(driver, Duration.ofSeconds(30)).login(EMAIL, PASSWORD);
            System.out.println("[OK] Login");

            // 2) 모니터링 페이지 이동
            new MonitoringNavigator(driver, Duration.ofSeconds(30)).goToMonitoring();
            System.out.println("[OK] Navigated to monitoring page");

            // 3) Export 다운로드 완료 대기
            var downloader = new ExportDownloader(driver, Duration.ofSeconds(30));
            var file = downloader.clickAndWaitForDownload(downloadDir, Duration.ofSeconds(120));
            System.out.println("[OK] Downloaded: " + file.toAbsolutePath());

            // 4) 크롬 브라우저 종료
            driver.quit();

            // TODO: 서버 업로드 로직 연결

        } finally {

        }
    }

}
