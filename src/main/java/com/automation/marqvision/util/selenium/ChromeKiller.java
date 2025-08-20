package com.automation.marqvision.util.selenium;

import java.nio.file.Path;

public final class ChromeKiller {
    private ChromeKiller() {}

    public static void killByUserDataDir(Path profileDir) {
        if (profileDir == null) return;
        String needle = "--user-data-dir=" + profileDir.toAbsolutePath();

        try {
            if (isWindows()) {
                String ps = """
          $needle = [Regex]::Escape("%s")
          Get-CimInstance Win32_Process |
            Where-Object { $_.CommandLine -match $needle } |
            ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
          """.formatted(needle.replace("\\", "\\\\"));
                new ProcessBuilder("powershell","-NoProfile","-Command", ps)
                        .inheritIO().start().waitFor();
            } else {
                String sh = "pkill -f \"" + needle.replace("\"","\\\"") + "\" || true";
                new ProcessBuilder("bash","-lc", sh).inheritIO().start().waitFor();
            }
        } catch (Exception ignored) {}
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
