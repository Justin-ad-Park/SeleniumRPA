package com.automation.marqvision.util;

public class EnvUtils {

    public static String requireEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            v = System.getProperty(key); // -DMARQVISION_EMAIL=... 지원
        }
        if (v == null || v.isBlank()) {
            System.err.println("[ERR] Missing env or system property: " + key);
            System.err.println("Provide either environment variables or -D properties, e.g.:");
            System.err.println("  macOS/Linux:");
            System.err.println("    export MARQVISION_EMAIL=...; export MARQVISION_PASSWORD=...; ./gradlew run");
            System.err.println("  OR");
            System.err.println("    ./gradlew run -DMARQVISION_EMAIL=... -DMARQVISION_PASSWORD=...");
            System.err.println("  Windows (PowerShell):");
            System.err.println("    $env:MARQVISION_EMAIL=...; $env:MARQVISION_PASSWORD=...; gradlew.bat run");
            System.err.println("  OR");
            System.err.println("    gradlew.bat run -DMARQVISION_EMAIL=... -DMARQVISION_PASSWORD=...");
            System.exit(1);
        }
        return v;
    }
}