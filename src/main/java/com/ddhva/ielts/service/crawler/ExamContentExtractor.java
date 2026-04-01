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

import java.util.Arrays;
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
                    ".question-content-left",
                    ".form-content",
                    ".test-content",
                    ".content-left",
                    ".context-content",
                    ".context-wrapper",
                    ".passage-content",
                    ".reading-passage",
                    ".question-passage",
                    ".test-passage",
                    "[class*='passage']",
                    "[class*='content-left']",
                    "[class*='question-form']",
                    ".col-left",
                    ".left-panel",
                    ".question-body > div:first-child"
            )) {
                Locator el = page.locator(sel).first();
                if (el.count() > 0) {
                    String html = extractCleanedSectionHtml(el);
                    if (StringUtils.hasText(html)) return html;
                }
            }
        } catch (Exception ex) {
            log.warn("extractPassageHtml lỗi: {}", ex.getMessage());
        }
        return null;
    }

    private String extractCleanedSectionHtml(Locator el) {
        try {
            String html = el.evaluate("""
                node => {
                    const clone = node.cloneNode(true);

                    const removeSelectors = [
                        '.question-wrapper',
                        '.question-item',
                        '.question',
                        '.question-content',
                        '.question-title',
                        '.question-number',
                        '.q-num',
                        '.audio-player',
                        'audio',
                        'source',
                        'button',
                        '[role="button"]',
                        'input',
                        'textarea',
                        'select',
                        'label',
                        '.play',
                        '.mute',
                        '.settings',
                        '.controls'
                    ];

                    removeSelectors.forEach(sel => {
                        clone.querySelectorAll(sel).forEach(el => el.remove());
                    });

                    return clone.innerHTML || clone.textContent || '';
                }
            """).toString();

            if (!StringUtils.hasText(html)) return null;
            return preserveLayoutText(html);
        } catch (Exception ex) {
            try {
                String text = el.innerText();
                if (!StringUtils.hasText(text)) return null;
                return preserveLayoutText(text);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String extractPreservedLayoutText(Locator el) {
        try {
            String text = el.innerText();
            if (!StringUtils.hasText(text)) return null;
            return preserveLayoutText(text);
        } catch (Exception ex) {
            return null;
        }
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
        try {
            // Ưu tiên selector cụ thể trước
            for (String sel : List.of(
                    ".question-content",
                    ".question-title",
                    ".question-text",
                    "h4", "h5")) {
                Locator el = wrapper.locator(sel).first();
                if (el.count() > 0) {
                    String text = cleanQuestionContent(CrawlerUtils.sanitize(el.innerText()));
                    if (StringUtils.hasText(text) && text.length() < 500) // chặn passage dài
                        return text;
                }
            }

            // Fallback: lấy text của wrapper nhưng giới hạn độ dài
            String fullText = cleanQuestionContent(CrawlerUtils.sanitize(wrapper.innerText()));
            if (StringUtils.hasText(fullText)) {
                // Chỉ lấy dòng đầu tiên có nghĩa (câu hỏi thường ngắn)
                String firstLine = Arrays.stream(fullText.split("\\n"))
                        .map(String::trim)
                        .filter(line -> line.length() > 3 && line.length() < 300)
                        .findFirst()
                        .orElse(fullText);
                return firstLine.length() > 500 ? firstLine.substring(0, 500) : firstLine;
            }
        } catch (Exception ex) {
            log.warn("extractQuestionContent lỗi: {}", ex.getMessage());
        }
        return "";
    }

    private String cleanQuestionContent(String text) {
        if (!StringUtils.hasText(text)) return "";

        String cleaned = text.replace('\u00A0', ' ');
        cleaned = cleaned.replaceAll("(?is)\\s+(A|B|C)\\s*[\\.)]\\s*.*$", "");
        cleaned = cleaned.replaceAll("(?is)\\s+\\d+\\s*[\\.)]\\s*.*$", "");

        cleaned = cleaned.replaceAll("(?m)^\\s*[A-C]\\s*[\\.)]\\s*.*$", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*\\d+\\s*[\\.)]\\s*.*$", "");

        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    public String extractQuestionDisplayText(Locator wrapper, int questionNumber) {
        try {
            String html = wrapper.innerHTML();
            if (!StringUtils.hasText(html)) {
                return preserveLayoutText(wrapper.innerText());
            }

            String text = html;

            text = text.replaceAll("(?i)<input[^>]*>", " ____" + questionNumber + "____ ");
            text = text.replaceAll("(?i)<textarea[^>]*>.*?</textarea>", " ____" + questionNumber + "____ ");
            text = text.replaceAll("(?i)<select[^>]*>.*?</select>", " ____" + questionNumber + "____ ");

            text = text.replaceAll("(?i)<br\\s*/?>", "\n");
            text = text.replaceAll("(?i)</p>", "\n");
            text = text.replaceAll("(?i)</div>", "\n");
            text = text.replaceAll("(?i)</li>", "\n");

            text = text.replaceAll("<[^>]+>", " ");
            text = preserveLayoutText(text);

            if (StringUtils.hasText(text)) {
                return text;
            }
        } catch (Exception ex) {
            log.warn("extractQuestionDisplayText lỗi: {}", ex.getMessage());
        }

        return preserveLayoutText(wrapper.innerText());
    }

    private String preserveLayoutText(String input) {
        if (input == null) return "";
        String text = input.replace('\u00A0', ' ');
        text = text.replaceAll("[ \\t\\x0B\\f\\r]+", " ");
        text = text.replaceAll("\\n[ \\t\\x0B\\f\\r]+", "\n");
        text = text.replaceAll("[ \\t\\x0B\\f\\r]+\\n", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }

    public String extractQuestionAnswerContent(Locator wrapper, int questionNumber) {
        String content = extractQuestionContent(wrapper);
        if (!StringUtils.hasText(content)) return String.valueOf(questionNumber);
        return content;
    }

    public String extractSectionImageUrl(Page page) {
        try {
            for (String sel : List.of(
                    ".question-content-left img",
                    ".form-content img",
                    ".test-content img",
                    ".content-left img",
                    ".question-form img",
                    ".question-body img",
                    "img[src*='media']",
                    "img[src*='upload']",
                    "img:not([src*='icon']):not([src*='logo'])")) {
                Locator img = page.locator(sel).first();
                if (img.count() > 0) {
                    String src = img.getAttribute("src");
                    if (StringUtils.hasText(src)) {
                        return CrawlerUtils.toAbsoluteUrl(requireBaseUrl(), src);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("extractSectionImageUrl lỗi: {}", ex.getMessage());
        }
        return null;
    }

    public Integer extractQuestionNumber(Locator wrapper, int fallbackIndex) {
        try {
            // 1. data-question-id attribute
            String dataId = wrapper.getAttribute("data-question-id");
            if (StringUtils.hasText(dataId)) {
                String digits = dataId.replaceAll("[^0-9]", "");
                if (!digits.isBlank()) return Integer.parseInt(digits);
            }

            // 2. id="question-N"
            String id = wrapper.getAttribute("id");
            if (StringUtils.hasText(id)) {
                Matcher m = Pattern.compile("question-(\\d+)").matcher(id);
                if (m.find()) return Integer.parseInt(m.group(1));
            }

            // 3. .question-number element — CHỈ lấy nếu text toàn số
            Locator numEl = wrapper.locator(".question-number, .q-num").first();
            if (numEl.count() > 0) {
                String text = CrawlerUtils.sanitize(numEl.innerText()).trim();
                // Chỉ parse nếu text ngắn (tránh lấy nhầm content câu hỏi)
                if (!text.isBlank() && text.length() < 20) {
                    Matcher m = QUESTION_NUMBER_PATTERN.matcher(text);
                    if (m.find()) return Integer.parseInt(m.group(1));
                }
            }
        } catch (NumberFormatException ex) {
            log.warn("extractQuestionNumber: không parse được số, dùng fallback {}", fallbackIndex);
        } catch (Exception ignored) {}

        return fallbackIndex; // fallback an toàn
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