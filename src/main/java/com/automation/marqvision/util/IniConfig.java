package com.automation.marqvision.util;


import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public final class IniConfig {
    private IniConfig() {}

    public static final class UiSettings {
        public final int x, y, width, height;
        public final boolean headless; // invisible=true -> headless=true

        public UiSettings(int x, int y, int width, int height, boolean headless) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.headless = headless;
        }

        @Override public String toString() {
            return "UiSettings{x=" + x + ", y=" + y + ", width=" + width + ", height=" + height +
                    ", headless=" + headless + '}';
        }
    }

    /** 기본값 */
    private static final int    DEF_X = 60;
    private static final int    DEF_Y = 60;
    private static final int    DEF_W = 1024;
    private static final int    DEF_H = 768;
    private static final boolean DEF_HEADLESS = true; // invisible=true

    /**
     * 우선순위대로 설정 파일을 찾고 로드:
     *  1) -Dmarq.config=/path/to/config.ini (시스템 프로퍼티)
     *  2) ./config.ini (현재 작업 디렉터리)
     *  3) ~/.marqvision/config.ini
     * 없거나 오류면 기본값 반환.
     */
    public static UiSettings load() {
        String sysProp = System.getProperty("marq.config");
        Path cand =
                (sysProp != null && !sysProp.isBlank()) ? Paths.get(sysProp)
                        : Files.exists(Paths.get("config.ini")) ? Paths.get("config.ini")
                        : Paths.get(System.getProperty("user.home"), ".marqvision", "config.ini");

        return loadFromPath(cand);
    }

    /** 주어진 경로에서 읽되, 파일이 없거나 읽기 실패 시 기본값을 반환 */
    public static UiSettings loadFromPath(Path iniPath) {
        int x = DEF_X, y = DEF_Y, w = DEF_W, h = DEF_H;
        boolean headless = DEF_HEADLESS;

        try {
            if (iniPath != null && Files.exists(iniPath)) {
                List<String> lines = Files.readAllLines(iniPath);
                for (String raw : lines) {
                    if (raw == null) continue;
                    String line = stripComments(raw).trim();
                    if (line.isEmpty()) continue;
                    int eq = line.indexOf('=');
                    if (eq <= 0) continue;

                    String key = line.substring(0, eq).trim();
                    String val = line.substring(eq + 1).trim();

                    switch (key) {
                        case "positionX" -> x = parseInt(val, DEF_X);
                        case "positionY" -> y = parseInt(val, DEF_Y);
                        case "width"     -> w = parseInt(val, DEF_W);
                        case "height"    -> h = parseInt(val, DEF_H);
                        case "invisible" -> headless = parseBool(val, DEF_HEADLESS); // true/1/yes/on
                        default -> { /* ignore unknown */ }
                    }
                }
            }
        } catch (IOException ignored) {
            // 파일 없거나 읽기 실패 → 기본값 유지
        }

        // 간단한 유효성 보정
        if (w < 200) w = DEF_W;
        if (h < 200) h = DEF_H;

        return new UiSettings(x, y, w, h, headless);
    }

    private static String stripComments(String s) {
        String t = s;
        int i1 = t.indexOf("//");
        int i2 = t.indexOf('#');
        int cut = -1;
        if (i1 >= 0 && i2 >= 0) cut = Math.min(i1, i2);
        else if (i1 >= 0) cut = i1;
        else if (i2 >= 0) cut = i2;
        return (cut >= 0) ? t.substring(0, cut) : t;
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static boolean parseBool(String s, boolean def) {
        if (s == null) return def;
        String v = s.trim().toLowerCase();
        return switch (v) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> def;
        };
    }
}