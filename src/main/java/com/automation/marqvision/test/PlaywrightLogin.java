package com.automation.marqvision.test;

import com.automation.marqvision.util.EnvUtils;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class PlaywrightLogin {
    public static void main(String[] args) {
        final String EMAIL = EnvUtils.requireEnv("MARQVISION_EMAIL");
        final String PASSWORD = EnvUtils.requireEnv("MARQVISION_PASSWORD");

        if (EMAIL == null || PASSWORD == null) {
            System.err.println("Set MARQVISION_EMAIL and MARQVISION_PASSWORD env vars.");
            System.exit(1);
        }

        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext ctx = browser.newContext(new Browser.NewContextOptions().setAcceptDownloads(true));
            Page page = ctx.newPage();

            try {
                // 1) 진입
                page.navigate("https://brand.marqvision.com/login",
                        new Page.NavigateOptions().setTimeout(60_000));

                // 2) 로그인 폼 대기
                page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                                .setName(Pattern.compile("sign\\s*into\\s*your\\s*account", Pattern.CASE_INSENSITIVE)))
                        .waitFor(new Locator.WaitForOptions().setTimeout(30_000));

                // 3) 입력
                Locator email = page.getByLabel("Email address", new Page.GetByLabelOptions().setExact(false));
                if (!email.isVisible()) email = page.locator("input[type='email'], input[name='email'], input[placeholder*='Email' i]");
                email.fill(EMAIL);

                Locator pwd = page.getByLabel("Password", new Page.GetByLabelOptions().setExact(false));
                if (!pwd.isVisible()) pwd = page.locator("input[type='password'], input[name='password'], input[placeholder*='Password' i]");
                pwd.fill(PASSWORD);

                Locator remember = page.getByLabel("Remember me", new Page.GetByLabelOptions().setExact(false));
                if (remember.isVisible()) remember.check();

                // 4) 버튼 탐색 → final 로 고정
                Locator candidate = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in"));
                if (!candidate.isVisible()) {
                    candidate = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                            .setName(Pattern.compile("Sign\\s*in|Log\\s*in", Pattern.CASE_INSENSITIVE)));
                }
                if (!candidate.isVisible()) {
                    candidate = page.locator("button[type='submit'], button[name='login']");
                }
                final Locator signInBtn = candidate;

                // 5) 클릭과 동시에 네비게이션 대기 (버전 호환성 가장 좋음)
                page.waitForNavigation(() -> {
                    signInBtn.click(new Locator.ClickOptions().setTimeout(30_000));
                });

                // 6) 성공 판정 (여전히 /login 이면 실패로 간주)
                page.waitForURL(u -> true, new Page.WaitForURLOptions().setTimeout(30_000)); // URL 안정화
                if (page.url().contains("/login")) {
                    throw new RuntimeException("Login appears to have failed (still on /login). Current URL: " + page.url());
                }

                System.out.println("[OK] Logged in. URL: " + page.url());

                // (옵션) 세션 저장 → 다음 실행에서 재사용
                ctx.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("storageState.json")));
                System.out.println("[OK] storageState.json saved.");

            } catch (Throwable t) {
                Path shot = Paths.get("screenshots",
                        "login-failed-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".png");
                page.screenshot(new Page.ScreenshotOptions().setPath(shot).setFullPage(true));
                System.err.println("[ERR] Login failed. Screenshot: " + shot);
                t.printStackTrace();
                System.exit(2);
            } finally {
                ctx.close();
                browser.close();
            }
        }
    }
}
