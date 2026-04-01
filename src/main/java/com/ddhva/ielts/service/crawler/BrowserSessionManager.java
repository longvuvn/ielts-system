package com.ddhva.ielts.service.crawler;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.util.CrawlerUtils;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrowserSessionManager {

    private final IeltsUpCrawlerConfig config;
    private final PlaywrightFactory playwrightFactory;

    /**
     * Tạo BrowserContext đã được xác thực.
     * Thử load session từ file trước, nếu hết hạn thì login lại.
     */
    public BrowserContext createAuthenticatedContext(Browser browser,
                                                     AtomicBoolean acceptDialogEnabled) {
        Path sessionPath = Paths.get(playwrightFactory.sessionFile());

        if (Files.exists(sessionPath)) {
            try {
                BrowserContext ctx = playwrightFactory.newContext(
                        browser, sessionPath, acceptDialogEnabled);

                Page probe = ctx.newPage();
                try {
                    probe.navigate(requireBaseUrl() + "/dashboard/",
                            new Page.NavigateOptions()
                                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                                    .setTimeout(30_000));
                } catch (Exception navEx) {
                    log.warn("Probe navigate timeout, kiểm tra URL: {}", probe.url());
                }

                String currentUrl = probe.url();
                boolean valid = StringUtils.hasText(currentUrl)
                        && !currentUrl.contains("/login")
                        && !currentUrl.contains("accounts.google.com")
                        && !currentUrl.equals("about:blank");
                probe.close();

                if (valid) {
                    log.info("Session hợp lệ, dùng lại từ {}", playwrightFactory.sessionFile());
                    return ctx;
                }
                log.info("Session hết hạn, cần login lại");
                ctx.close();
                Files.deleteIfExists(sessionPath);
            } catch (Exception ex) {
                log.warn("Không load được session ({}), sẽ login lại", ex.getMessage());
            }
        }
        return loginManuallyAndSaveSession(browser, sessionPath, acceptDialogEnabled);
    }

    /**
     * Kiểm tra xem page hiện tại có phải login page không.
     * Nếu có → xóa session và yêu cầu login lại.
     */
    public void ensureLoggedIn(Page page, BrowserContext ctx,
                               AtomicBoolean acceptDialogEnabled) {
        if (!CrawlerUtils.isLoginPage(page)) return;
        log.warn("Phát hiện trang login giữa chừng — xóa session và yêu cầu đăng nhập lại");
        Path sessionPath = Paths.get(playwrightFactory.sessionFile());
        try { Files.deleteIfExists(sessionPath); } catch (Exception ignored) {}
        loginManuallyAndSaveSession(ctx.browser(), sessionPath, acceptDialogEnabled);
        throw new RuntimeException("Session đã hết hạn và đã được làm mới. Vui lòng thử lại.");
    }


    private BrowserContext loginManuallyAndSaveSession(Browser browser,
                                                       Path sessionPath,
                                                       AtomicBoolean acceptDialogEnabled) {
        log.info("Cần đăng nhập study4 lần đầu");
        log.info("Browser sẽ mở ra — hãy đăng nhập bằng Gmail");
        log.info("Sau khi vào trang chủ, ĐỪNG đóng browser");
        log.info("Chương trình sẽ tự tiếp tục");

        BrowserContext ctx = playwrightFactory.newFreshContext(browser, acceptDialogEnabled);
        Page page = ctx.newPage();

        page.navigate(requireBaseUrl() + "/login/",
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(60_000));
        page.waitForURL(
                url -> !url.contains("/login") && !url.contains("accounts.google.com"),
                new Page.WaitForURLOptions().setTimeout(300_000));

        log.info("Đăng nhập thành công! Đang lưu session...");
        ctx.storageState(new BrowserContext.StorageStateOptions().setPath(sessionPath));
        log.info("Session đã lưu vào {}", playwrightFactory.sessionFile());
        page.close();
        return ctx;
    }

    private String requireBaseUrl() {
        return CrawlerUtils.requireBaseUrl(config.getBaseUrl());
    }
}