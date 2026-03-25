package com.ddhva.ielts.service.crawler;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Component
@Slf4j
public class PlaywrightFactory {

    private static final String SESSION_FILE = "crawler-session.json";

    /**
     * Khởi động browser. Nếu đã có session file → headless, ngược lại → có UI để login.
     */
    public Browser launchBrowser(Playwright playwright) {
        boolean hasSession = Files.exists(Paths.get(SESSION_FILE));
        return playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setChannel("msedge")
                        .setHeadless(hasSession)
                        .setArgs(List.of(
                                "--disable-blink-features=AutomationControlled",
                                "--disable-infobars"
                        ))
        );
    }

    /**
     * Tạo context mới với user-agent và viewport chuẩn.
     * Gắn thêm dialog handler từ bên ngoài.
     */
    public BrowserContext newContext(Browser browser,
                                     java.nio.file.Path storageStatePath,
                                     AtomicBoolean acceptDialogEnabled) {
        var opts = new Browser.NewContextOptions()
                .setUserAgent(userAgent())
                .setViewportSize(1280, 800);

        if (storageStatePath != null && Files.exists(storageStatePath))
            opts.setStorageStatePath(storageStatePath);

        BrowserContext ctx = browser.newContext(opts);
        ctx.setDefaultTimeout(60_000);
        ctx.setDefaultNavigationTimeout(60_000);
        registerDialogHandler(ctx, acceptDialogEnabled);
        return ctx;
    }

    /**
     * Context không có session (dùng khi login lần đầu).
     */
    public BrowserContext newFreshContext(Browser browser,
                                          AtomicBoolean acceptDialogEnabled) {
        BrowserContext ctx = browser.newContext(
                new Browser.NewContextOptions()
                        .setUserAgent(userAgent())
                        .setViewportSize(1280, 800));
        ctx.addInitScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        registerDialogHandler(ctx, acceptDialogEnabled);
        return ctx;
    }

    public String sessionFile() {
        return SESSION_FILE;
    }

    //private

    private void registerDialogHandler(BrowserContext ctx, AtomicBoolean acceptDialogEnabled) {
        ctx.onDialog(dialog -> {
            if ("beforeunload".equals(dialog.type())) {
                try {
                    if (acceptDialogEnabled != null && acceptDialogEnabled.get())
                        dialog.accept();
                    else
                        dialog.dismiss();
                } catch (Exception ignored) {}
            } else {
                try { dialog.dismiss(); } catch (Exception ignored) {}
            }
        });
    }

    private String userAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0";
    }
}
