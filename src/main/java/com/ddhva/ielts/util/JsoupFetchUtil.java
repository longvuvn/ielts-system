package com.ddhva.ielts.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JsoupFetchUtil {

    public Document fetch(String url) {
        try {
            log.debug("[FETCH] {}", url);
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                    .referrer("https://ielts-up.com")
                    .timeout(15000)
                    .get();
        } catch (Exception e) {
            log.error("[FETCH] Failed {}: {}", url, e.getMessage());
            return null;
        }
    }

    public void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}