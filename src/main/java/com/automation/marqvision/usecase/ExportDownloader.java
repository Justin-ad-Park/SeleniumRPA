package com.automation.marqvision.usecase;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.*;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;

import static com.automation.marqvision.util.selenium.SeleniumUtils.highlight;

/**
 * Export 버튼 클릭 후, 디렉터리를 polling 하여
 * - .crdownload 임시 파일 생성 감지
 * - 최종 파일명으로 rename 감지
 * - 최종 파일의 크기 안정화(2회 연속 동일) 확인
 * 로 다운로드 완료 여부를 판정한다.
 */
public class ExportDownloader {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ExportDownloader(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    public Path clickAndWaitForDownload(Path downloadDir, Duration timeout) {
        long start = System.currentTimeMillis();

        // 1) Export 버튼 클릭 (role=button + 텍스트 Export)
        By exportChip = By.xpath("//div[@role='button' and .//span[normalize-space()='Export']]");
        WebElement chip = wait.until(ExpectedConditions.elementToBeClickable(exportChip));
        highlight(driver, chip);
        try {
            chip.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chip);
        }

        // 2) 다운로드 완료까지 대기
        return awaitDownloadComplete(downloadDir, start, timeout);
    }

    /**
     * 디렉토리를 polling 하여 .crdownload → 최종 파일 완료까지 확인한다.
     */
    private Path awaitDownloadComplete(Path dir, long startMillis, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        Path candidateFinal = null;      // 최종 파일 Path
        Path candidateTemp  = null;      // .crdownload Path
        Long lastSize = null;            // 안정화 확인용 직전 사이즈
        int stableCount = 0;             // 사이즈 동일 카운트

        while (System.currentTimeMillis() < deadline) {
            try {
                // 최신 파일 스캔 (start 이후 생성/수정된 것들 중에서)
                Optional<Path> newest = Files.list(dir)
                        .filter(p -> Files.isRegularFile(p))
                        .filter(p -> {
                            try { return Files.getLastModifiedTime(p).toMillis() >= startMillis; }
                            catch (Exception e) { return false; }
                        })
                        .max(Comparator.comparingLong(p -> {
                            try { return Files.getLastModifiedTime(p).toMillis(); }
                            catch (Exception e) { return 0L; }
                        }));

                if (newest.isPresent()) {
                    Path p = newest.get();
                    String name = p.getFileName().toString();

                    // 1) .crdownload가 보이면 임시 후보로 기록하고, 대응되는 최종 파일명을 계산
                    if (name.endsWith(".crdownload") || name.endsWith(".tmp")) {
                        candidateTemp = p;
                        candidateFinal = stripTempExtension(p); // temp 확장자 제거한 최종 예상 파일명
                        lastSize = null;
                        stableCount = 0;
                    } else {
                        // 2) 비임시 파일이 새로 생겼거나(작은 파일은 .crdownload 없이 바로 떨어지기도 함),
                        //    temp->final rename 후 최종 파일이 관측됨
                        candidateFinal = p;
                    }
                }

                // 최종 파일이 정해졌다면 크기 안정화 체크
                if (candidateFinal != null && Files.exists(candidateFinal)) {
                    long size = Files.size(candidateFinal);
                    if (size > 0) {
                        if (lastSize != null && size == lastSize) {
                            stableCount++;
                        } else {
                            stableCount = 0;
                        }
                        lastSize = size;

                        // 임시 파일이 더는 없고, 사이즈가 2회 연속 동일하면 완료로 간주
                        if (!anyTempFiles(dir) && stableCount >= 1) { // 2번 체크(0→1→완료)
                            return candidateFinal;
                        }
                    }
                }

                Thread.sleep(300);
            } catch (Exception ignored) {
                try { Thread.sleep(300); } catch (InterruptedException ignored2) {}
            }
        }
        throw new RuntimeException("다운로드 완료를 시간 내에 확인하지 못했습니다. dir=" + dir);
    }

    /** 디렉토리에 남아있는 임시(.crdownload/.tmp) 파일이 있는지 */
    private boolean anyTempFiles(Path dir) {
        try {
            return Files.list(dir)
                    .anyMatch(p -> {
                        String n = p.getFileName().toString();
                        return Files.isRegularFile(p) && (n.endsWith(".crdownload") || n.endsWith(".tmp"));
                    });
        } catch (Exception e) {
            return false;
        }
    }

    /** foo.xlsx.crdownload → foo.xlsx 로 예상 최종 파일명을 구함 */
    private Path stripTempExtension(Path tempPath) {
        String n = tempPath.getFileName().toString();
        if (n.endsWith(".crdownload")) {
            String base = n.substring(0, n.length() - ".crdownload".length());
            return tempPath.getParent().resolve(base);
        } else if (n.endsWith(".tmp")) {
            String base = n.substring(0, n.length() - ".tmp".length());
            return tempPath.getParent().resolve(base);
        }
        return tempPath;
    }
}
