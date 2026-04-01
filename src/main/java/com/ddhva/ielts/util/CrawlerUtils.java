package com.ddhva.ielts.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CrawlerUtils {

    private CrawlerUtils() {}

    // Delay

    public static void humanDelay(long minMs, long maxMs) {
        try {
            long delay = minMs + (long) (Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //String utils

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    public static String normalize(String s) {
        if (!StringUtils.hasText(s)) return "";
        return Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static boolean isMatch(String a, String b) {
        if (!StringUtils.hasText(a) || !StringUtils.hasText(b)) return false;
        String na = normalize(a), nb = normalize(b);
        if (na.equals(nb)) return true;
        if (na.length() >= 4 && nb.length() >= 4)
            return na.contains(nb) || nb.contains(na);
        return false;
    }

    public static String toAbsoluteUrl(String base, String href) {
        if (!StringUtils.hasText(href)) return href;
        String h = href.trim();
        if (h.startsWith("//"))   return "https:" + h;
        if (h.startsWith("http")) return h;
        if (!h.startsWith("/"))   h = "/" + h;
        return base + h;
    }

    public static int extractInt(Pattern p, String text, int fallback) {
        if (!StringUtils.hasText(text)) return fallback;
        Matcher m = p.matcher(text);
        if (!m.find()) return fallback;
        try { return Integer.parseInt(m.group(1)); } catch (Exception e) { return fallback; }
    }

    public static int countNonNull(List<?> list) {
        return (int) list.stream().filter(Objects::nonNull).count();
    }

    //URL helpers

    public static String requireBaseUrl(String url) {
        if (!StringUtils.hasText(url))
            throw new IllegalStateException("crawler.base-url must not be blank");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url.trim();
    }

    public static String normalizeTestsPath(String path) {
        if (!StringUtils.hasText(path)) return "/tests/ielts/";
        String n = path.trim();
        if (!n.startsWith("/")) n = "/" + n;
        if (!n.endsWith("/"))   n = n + "/";
        return n;
    }

    // Playwright helpers

    public static Page.NavigateOptions domLoaded() {
        return new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(60_000);
    }

    public static String innerText(Page page, String selector) {
        try {
            var el = page.locator(selector).first();
            return el.count() > 0 ? sanitize(el.innerText()) : "";
        } catch (Exception ex) { return ""; }
    }

    public static boolean isLoginPage(Page page) {
        try {
            String url   = page.url().toLowerCase();
            String title = page.title().toLowerCase();
            return url.contains("/login")
                    || title.contains("login")
                    || title.contains("log in");
        } catch (Exception e) { return false; }
    }

    //JS builders

    public static String buildFillAllInputsJs() {
        return "() => {" +
                "  var chars = 'abcdefghijklmnopqrstuvwxyz';" +
                "  function rand() {" +
                "    var len = 3 + Math.floor(Math.random() * 6);" +
                "    var s = '';" +
                "    for (var i=0;i<len;i++) s += chars[Math.floor(Math.random()*chars.length)];" +
                "    return s;" +
                "  }" +
                "  var filled = 0;" +
                "  document.querySelectorAll('input[type=text],input[type=number],textarea').forEach(function(el) {" +
                "    if (el.disabled || el.readOnly) return;" +
                "    el.value = rand();" +
                "    el.dispatchEvent(new Event('input',{bubbles:true}));" +
                "    el.dispatchEvent(new Event('change',{bubbles:true}));" +
                "    filled++;" +
                "  });" +
                "  var rg = {};" +
                "  document.querySelectorAll('input[type=radio]:not([disabled])').forEach(function(r) {" +
                "    if (!rg[r.name]) rg[r.name]=[];" +
                "    rg[r.name].push(r);" +
                "  });" +
                "  Object.values(rg).forEach(function(g) {" +
                "    if (!g.find(function(r){return r.checked;})) {" +
                "      g[0].checked=true; g[0].dispatchEvent(new Event('change',{bubbles:true}));" +
                "    }" +
                "  });" +
                "  return filled;" +
                "}";
    }
}
