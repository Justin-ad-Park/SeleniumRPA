package com.automation.marqvision.util;

public class EnvUtils {

    public static String requireEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            System.err.println("[ERR] Missing environment variable: " + key);
            System.err.println("Set environment variables before running, for example:");
            System.err.println("  macOS/Linux (bash/zsh):");
            System.err.println("    export MARQVISION_EMAIL=\"your_email@example.com\"");
            System.err.println("    export MARQVISION_PASSWORD=\"your_password\"");
            System.err.println("    ./gradlew run");
            System.err.println("  Windows (PowerShell):");
            System.err.println("    $env:MARQVISION_EMAIL = \"your_email@example.com\"");
            System.err.println("    $env:MARQVISION_PASSWORD = \"your_password\"");
            System.err.println("    gradlew.bat run");
            System.exit(1);
        }
        return v;
    }
}
