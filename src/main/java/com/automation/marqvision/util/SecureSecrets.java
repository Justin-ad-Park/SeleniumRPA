package com.automation.marqvision.util;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SecureSecrets {

    public record Cred(String email, String password) {}

    // 1) OS 보안 저장소에서 읽기
    public static Cred loadFromOsStore() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("mac")) return loadFromMacKeychain("marqvision", "marqvision.login");
            if (os.contains("win")) return loadFromWindowsCredMan("marqvision.login");
        } catch (Exception ignored) {}
        return null;
    }

    // 2) env / system property 폴백
    public static Cred loadFromEnvOrProp() {
        String email = firstNonBlank(System.getenv("MARQVISION_EMAIL"), System.getProperty("MARQVISION_EMAIL"));
        String pass  = firstNonBlank(System.getenv("MARQVISION_PASSWORD"), System.getProperty("MARQVISION_PASSWORD"));
        if (isBlank(email) || isBlank(pass)) return null;
        return new Cred(email, pass);
    }

    // JSON은 {"email":"...","password":"..."} 최소 형태만 가정 (간단 파서)
    private static Cred parseJsonCred(String json) {
        if (isBlank(json)) return null;
        String e = extract(json, "\"email\"\\s*:\\s*\"([^\"]+)\"");
        String p = extract(json, "\"password\"\\s*:\\s*\"([^\"]+)\"");
        return (e != null && p != null) ? new Cred(e, p) : null;
    }

    private static Cred loadFromMacKeychain(String account, String service) throws IOException, InterruptedException {
        Process p = new ProcessBuilder("security", "find-generic-password", "-a", account, "-s", service, "-w")
                .redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        int code = p.waitFor();
        if (code != 0 || isBlank(out)) return null;
        return parseJsonCred(out);
    }

    private static Cred loadFromWindowsCredMan(String target) throws IOException, InterruptedException {
        String ps = """
      $m = Get-Module -ListAvailable CredentialManager;
      if (-not $m) { Write-Error 'CredentialManager module missing'; exit 3 }
      $c = Get-StoredCredential -Target '%s';
      if (-not $c) { exit 2 }
      $plain = $c.GetNetworkCredential().Password;
      [Console]::Out.Write($plain)
      """.formatted(target);
        Process p = new ProcessBuilder("powershell", "-NoProfile", "-Command", ps)
                .redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        int code = p.waitFor();
        if (code != 0 || isBlank(out)) return null;
        return parseJsonCred(out);
    }

    // Utils
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String firstNonBlank(String a, String b) { return !isBlank(a) ? a : (!isBlank(b) ? b : null); }
    private static String extract(String src, String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(src);
        return m.find() ? m.group(1) : null;
    }
}