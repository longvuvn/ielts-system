package com.ddhva.ielts.service.crawler;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.enums.QuestionType;
import com.ddhva.ielts.util.CrawlerUtils;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamContentExtractor {

    private static final Pattern QUESTION_NUMBER_PATTERN =
            Pattern.compile("^\\s*(?:câu\\s*)?(\\d+)[.:\\s]", Pattern.CASE_INSENSITIVE);

    private final IeltsUpCrawlerConfig config;

    // Passage
    public String extractPassageHtml(Page page) {
        try {
            for (String sel : List.of(
                    ".context-content",
                    ".context-wrapper",
                    ".passage-content",
                    ".reading-passage",
                    ".question-passage",
                    ".test-passage",
                    "[class*='passage']",
                    ".question-content-left",
                    ".form-content",
                    ".test-content",
                    ".content-left",
                    "[class*='content-left']",
                    "[class*='question-form']",
                    ".col-left",
                    ".left-panel",
                    ".question-body > div:first-child"
            )) {
                Locator el = page.locator(sel).first();
                if (el.count() > 0) {
                    String html = (String) page.evaluate(
                            "el => el.innerHTML", el.elementHandle());
                    if (StringUtils.hasText(html)) return html;
                }
            }
        } catch (Exception ex) {
            log.warn("extractPassageHtml lỗi: {}", ex.getMessage());
        }
        return null;
    }

    public String extractInstruction(Page page) {
        try {
            for (String sel : List.of(
                    ".question-instruction",
                    ".instruction",
                    "[class*='instruction']",
                    "p:has-text('Write'),p:has-text('Choose'),p:has-text('Complete')")) {
                Locator el = page.locator(sel).first();
                if (el.count() > 0) {
                    String text = CrawlerUtils.sanitize(el.innerText());
                    if (StringUtils.hasText(text)) return text;
                }
            }
        } catch (Exception ex) {
            log.warn("extractInstruction lỗi: {}", ex.getMessage());
        }
        return null;
    }

    //Audio
    public String extractAudioUrl(Page page) {
        try {
            for (String sel : List.of(
                    "audio[src]", "audio source[src]",
                    "[data-audio-url]", ".audio-player[src]",
                    "source[type*='audio']")) {
                Locator el = page.locator(sel).first();
                if (el.count() > 0) {
                    String src = el.getAttribute("src");
                    if (!StringUtils.hasText(src))
                        src = el.getAttribute("data-audio-url");
                    if (StringUtils.hasText(src))
                        return CrawlerUtils.toAbsoluteUrl(requireBaseUrl(), src);
                }
            }
            Object raw = page.evaluate(
                    "() => {" +
                            "  var a = document.querySelector('audio');" +
                            "  if (a && a.src) return a.src;" +
                            "  var s = document.querySelector('source[src]');" +
                            "  if (s) return s.src;" +
                            "  var el = document.querySelector('[data-audio]');" +
                            "  return el ? (el.dataset.audio || el.dataset.audioUrl || '') : '';" +
                            "}"
            );
            if (raw instanceof String s && StringUtils.hasText(s)) return s;
        } catch (Exception ex) {
            log.warn("extractAudioUrl lỗi: {}", ex.getMessage());
        }
        return null;
    }

    // Question

    public String extractQuestionContent(Locator wrapper) {
        for (String sel : List.of(".question-content", ".question-title", "p", "h4")) {
            Locator el = wrapper.locator(sel).first();
            if (el.count() > 0) {
                String text = CrawlerUtils.sanitize(el.innerText());
                if (!text.isBlank()) return text;
            }
        }
        return CrawlerUtils.sanitize(wrapper.innerText());
    }

    public String extractQuestionImageUrl(Locator wrapper) {
        try {
            for (String sel : List.of(
                    ".question-content img",
                    ".question-image img",
                    "img[src*='media']",
                    "img[src*='upload']",
                    "img:not([src*='icon']):not([src*='logo'])")) {
                Locator img = wrapper.locator(sel).first();
                if (img.count() > 0) {
                    String src = img.getAttribute("src");
                    if (StringUtils.hasText(src))
                        return CrawlerUtils.toAbsoluteUrl(requireBaseUrl(), src);
                }
            }
        } catch (Exception ex) {
            log.warn("extractQuestionImageUrl lỗi: {}", ex.getMessage());
        }
        return null;
    }

    public Integer extractQuestionNumber(Locator wrapper, int fallbackIndex) {
        try {
            String dataId = wrapper.getAttribute("data-question-id");
            if (StringUtils.hasText(dataId)) {
                try { return Integer.parseInt(dataId.replaceAll("[^0-9]", "")); }
                catch (Exception ignored) {}
            }
            String id = wrapper.getAttribute("id");
            if (StringUtils.hasText(id)) {
                Matcher m = Pattern.compile("question-(\\d+)").matcher(id);
                if (m.find()) return Integer.parseInt(m.group(1));
            }
            Locator numEl = wrapper.locator(".question-number, .q-num").first();
            if (numEl.count() > 0) {
                String text = CrawlerUtils.sanitize(numEl.innerText());
                if (!text.isBlank()) {
                    Matcher m = QUESTION_NUMBER_PATTERN.matcher(text);
                    if (m.find()) return Integer.parseInt(m.group(1));
                }
            }
        } catch (Exception ignored) {}
        return fallbackIndex;
    }

    public QuestionType detectQuestionType(Locator wrapper) {
        if (wrapper.locator("input[type=radio]").count()    > 0) return QuestionType.MULTIPLE_CHOICE;
        if (wrapper.locator("input[type=checkbox]").count() > 0) return QuestionType.MULTIPLE_ANSWER;
        return QuestionType.FILL_IN_BLANK;
    }

    //private

    private String requireBaseUrl() {
        return CrawlerUtils.requireBaseUrl(config.getBaseUrl());
    }
}