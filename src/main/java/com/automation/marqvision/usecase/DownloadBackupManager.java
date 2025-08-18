package com.automation.marqvision.usecase;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class DownloadBackupManager {

    private static final String BACKUP_DIR_NAME = "backup";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * 다운로드 폴더 내 기존 파일들을 바로 하위 "backup" 폴더로 이동
     * - 하위 디렉토리는 건너뜀(backup 폴더 포함)
     * - 파일명이 충돌하면 타임스탬프를 붙여서 이동
     * @return 이동한 파일 개수
     */
    public static int backupToSubfolder(Path downloadDir) {
        try {
            if (!Files.exists(downloadDir)) {
                Files.createDirectories(downloadDir);
            }
            Path backupDir = downloadDir.resolve(BACKUP_DIR_NAME);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            int moved = 0;
            try (Stream<Path> stream = Files.list(downloadDir)) {
                for (Path p : (Iterable<Path>) stream::iterator) {
                    if (Files.isDirectory(p)) {
                        // 하위 디렉토리는 건너뜀 (특히 backup 폴더)
                        continue;
                    }
                    // 대상 파일명
                    Path target = backupDir.resolve(p.getFileName());
                    // 충돌 시: name-YYYYMMDD-HHMMSS.ext 형태로 변경
                    if (Files.exists(target)) {
                        String fn = p.getFileName().toString();
                        int dot = fn.lastIndexOf('.');
                        String base = (dot > 0) ? fn.substring(0, dot) : fn;
                        String ext  = (dot > 0) ? fn.substring(dot)    : "";
                        String stamped = base + "-" + LocalDateTime.now().format(TS) + ext;
                        target = backupDir.resolve(stamped);
                        // 혹시 아주 드물게 같은 초에 또 충돌하면 숫자 접미사로 추가 회피
                        int i = 1;
                        while (Files.exists(target)) {
                            target = backupDir.resolve(base + "-" + LocalDateTime.now().format(TS) + "-" + (i++) + ext);
                        }
                    }
                    Files.move(p, target, StandardCopyOption.ATOMIC_MOVE);
                    moved++;
                }
            }
            return moved;
        } catch (IOException e) {
            throw new RuntimeException("백업 처리 중 오류: " + e.getMessage(), e);
        }
    }
}
