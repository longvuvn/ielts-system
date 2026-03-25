package com.ddhva.ielts.service.crawler;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.util.CrawlerUtils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamPageNavigator {

    private static final String PRACTICE_BTN_SELECTOR =
            "button:text-matches('LUYỆN TẬP|Luyện tập|Practice', 'i')";
    private static final String SUBMIT_BTN_SELECTOR =
            "#submit-test, button:text-matches('NỘP BÀI|Nộp bài|SUBMIT|Submit', 'i')";
    private static final Pattern TEST_LINK_PATTERN =
            Pattern.compile("/tests/(\\d+)/[^/]+/?$");
    private static final Pattern TEST_ID_PATTERN =
            Pattern.compile("/tests/(\\d+)/");

    private final IeltsUpCrawlerConfig config;

    //Collect links

    public Set<String> collectAllExamLinks(Page page) {
        String baseUrl = requireBaseUrl();
        String listUrl = baseUrl + CrawlerUtils.normalizeTestsPath(config.getTestsPath());

        page.navigate(listUrl, CrawlerUtils.domLoaded());
        int maxPage = detectMaxPage(page);
        if (config.getMaxPages() != null && config.getMaxPages() > 0)
            maxPage = Math.min(maxPage, config.getMaxPages());

        Set<String> links = new LinkedHashSet<>();
        extractLinksFromCurrentPage(page, baseUrl, links);
        for (int p = 2; p <= maxPage; p++) {
            page.navigate(listUrl + "?page=" + p, CrawlerUtils.domLoaded());
            extractLinksFromCurrentPage(page, baseUrl, links);
        }
        return links;
    }

    // Part values

    @SuppressWarnings("unchecked")
    public List<String> getPartValues(Page page) {
        try {
            page.locator("input[name=part], form[action*='practice']")
                    .first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(10_000));

            String js =
                    "() => {" +
                            "  var inputs = Array.from(document.querySelectorAll('input[name=part]'));" +
                            "  if (inputs.length > 0) return inputs.map(function(i){ return i.value; });" +
                            "  var forms = Array.from(document.querySelectorAll('form[action*=\"practice\"]'));" +
                            "  return forms.map(function(f) {" +
                            "    try { var u = new URL(f.action, location.href); return u.searchParams.get('part') || ''; }" +
                            "    catch(e) { return ''; }" +
                            "  }).filter(function(v){ return v.length > 0; });" +
                            "}";

            Object raw = page.evaluate(js);
            if (!(raw instanceof List<?> list)) return List.of();
            return list.stream()
                    .map(v -> v != null ? String.valueOf(v).trim() : "")
                    .filter(v -> !v.isBlank())
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("getPartValues lỗi: {}", ex.getMessage());
            return List.of();
        }
    }

    //Part selection

    public boolean checkOnlyOnePart(Page page, String partValue) {
        try {
            String js =
                    "function(partVal) {" +
                            "  var inputs = Array.from(document.querySelectorAll('input[name=part]'));" +
                            "  if (inputs.length === 0) return false;" +
                            "  inputs.forEach(function(inp) {" +
                            "    if (inp.checked) {" +
                            "      inp.checked = false;" +
                            "      inp.dispatchEvent(new Event('change', {bubbles:true}));" +
                            "    }" +
                            "  });" +
                            "  var target = inputs.find(function(inp){ return inp.value === partVal; });" +
                            "  if (!target) return false;" +
                            "  if (target.id) {" +
                            "    var lbl = document.querySelector('label[for=\"' + target.id + '\"]');" +
                            "    if (lbl) { lbl.click(); return true; }" +
                            "  }" +
                            "  target.checked = true;" +
                            "  target.dispatchEvent(new Event('change', {bubbles:true}));" +
                            "  return target.checked;" +
                            "}";
            Object result = page.evaluate(js, partValue);
            boolean ok = Boolean.TRUE.equals(result);
            log.debug("checkOnlyOnePart value='{}': {}", partValue, ok);
            return ok;
        } catch (Exception ex) {
            log.warn("checkOnlyOnePart lỗi (value='{}'): {}", partValue, ex.getMessage());
            return false;
        }
    }

    //Enter practice

    public String enterPractice(Page page, String examUrl) {
        try {
            Locator btn = page.locator(PRACTICE_BTN_SELECTOR).first();
            btn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(8_000));
            btn.scrollIntoViewIfNeeded();
            try {
                page.waitForNavigation(
                        new Page.WaitForNavigationOptions()
                                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                                .setTimeout(20_000),
                        btn::click);
            } catch (Exception e) {
                log.debug("waitForNavigation timeout, kiểm tra URL: {}", page.url());
            }
            String landedUrl = page.url();
            if (landedUrl.contains("/practice/")) return landedUrl;
            log.warn("Sau click không ra /practice/, URL: {}", landedUrl);
            return null;
        } catch (Exception ex) {
            log.warn("enterPractice lỗi: {}", ex.getMessage());
            return null;
        }
    }

    public String buildDirectPracticeUrl(String examUrl, String partValue) {
        Matcher m = TEST_ID_PATTERN.matcher(examUrl);
        if (m.find())
            return requireBaseUrl() + "/tests/" + m.group(1) + "/practice/?part=" + partValue;
        String base = examUrl.endsWith("/") ? examUrl : examUrl + "/";
        return base + "practice/?part=" + partValue;
    }

    //Submit

    public String submitPractice(Page page, String practiceUrl,
                                 AtomicBoolean acceptDialogEnabled) {
        try {
            int waitSec = config.getSubmitWaitMinSeconds() != null
                    ? config.getSubmitWaitMinSeconds() : 0;
            try {
                page.locator(SUBMIT_BTN_SELECTOR).first()
                        .waitFor(new Locator.WaitForOptions()
                                .setTimeout(Math.max(waitSec * 1000L + 5_000L, 10_000)));
            } catch (Exception e) {
                log.warn("Không tìm thấy submit button, thử tiếp");
            }
            if (waitSec > 0) {
                log.info("Chờ thêm {}s...", waitSec);
                Thread.sleep(waitSec * 1000L);
            }

            acceptDialogEnabled.set(true);
            try {
                page.waitForNavigation(
                        new Page.WaitForNavigationOptions()
                                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                                .setTimeout(60_000),
                        () -> page.evaluate(
                                "() => {" +
                                        "  var btn = document.querySelector('#submit-test');" +
                                        "  if (btn) { btn.click(); return; }" +
                                        "  var btns = Array.from(document.querySelectorAll('button'));" +
                                        "  var f = btns.find(function(b) { return /submit/i.test(b.innerText) || /n\\u1ED9p/i.test(b.innerText); });" +
                                        "  if (f) f.click();" +
                                        "}"
                        )
                );
            } finally {
                acceptDialogEnabled.set(false);
            }

            String currentUrl = page.url();
            if (CrawlerUtils.isLoginPage(page)) {
                log.warn("Sau submit bị redirect về login");
                return null;
            }
            if (currentUrl.contains("/results/")) {
                log.info("Trang kết quả: {}", currentUrl);
                return currentUrl;
            }
            page.waitForTimeout(1500);
            Locator resultLink = page.locator("a[href*='/results/']").first();
            if (resultLink.count() > 0) {
                String href = resultLink.getAttribute("href");
                page.navigate(CrawlerUtils.toAbsoluteUrl(requireBaseUrl(), href),
                        CrawlerUtils.domLoaded());
                return page.url();
            }
            Locator resultBtn = page.locator(
                    "a:text-matches('Xem chi tiết|Xem kết quả|Thống kê', 'i')").first();
            if (resultBtn.count() > 0) {
                String href = resultBtn.getAttribute("href");
                if (StringUtils.hasText(href)) {
                    page.navigate(CrawlerUtils.toAbsoluteUrl(requireBaseUrl(), href),
                            CrawlerUtils.domLoaded());
                    return page.url();
                }
            }
            log.warn("Không tìm thấy link kết quả. URL hiện tại: {}", currentUrl);
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception ex) {
            log.warn("Lỗi submit: {}", ex.getMessage());
            return null;
        } finally {
            acceptDialogEnabled.set(false);
        }
    }


    private void extractLinksFromCurrentPage(Page page, String baseUrl, Set<String> out) {
        Locator links = page.locator("a[href]");
        for (int i = 0; i < links.count(); i++) {
            String href = links.nth(i).getAttribute("href");
            if (href != null && TEST_LINK_PATTERN.matcher(href).find())
                out.add(CrawlerUtils.toAbsoluteUrl(baseUrl, href));
        }
    }

    private int detectMaxPage(Page page) {
        int max = 1;
        Locator links = page.locator("a[href*='?page=']");
        for (int i = 0; i < links.count(); i++) {
            String href = links.nth(i).getAttribute("href");
            if (href == null) continue;
            int idx = href.indexOf("?page=");
            try {
                int p = Integer.parseInt(href.substring(idx + 6).replaceAll("[^0-9].*", ""));
                max = Math.max(max, p);
            } catch (Exception ignored) {}
        }
        return max;
    }

    private String requireBaseUrl() {
        return CrawlerUtils.requireBaseUrl(config.getBaseUrl());
    }
}