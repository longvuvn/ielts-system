package com.ddhva.ielts.service.crawler.questions;

import com.ddhva.ielts.model.Answer;
import com.ddhva.ielts.model.Question;
import com.ddhva.ielts.repositories.AnswerRepository;
import com.ddhva.ielts.repositories.QuestionRepository;
import com.ddhva.ielts.util.CrawlerUtils;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnswerKeyScraper {

    private static final Pattern QUESTION_NUMBER_PATTERN =
            Pattern.compile("^\\s*(?:câu\\s*)?(\\d+)[.:\\s]", Pattern.CASE_INSENSITIVE);

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerApplier answerApplier;

    /**
     * Entry point: điều phối các strategy scrape đáp án từ trang results/details.
     */
    public int scrapeAndApply(Page page, String resultsUrl,
                              UUID sectionId,
                              List<Question> orderedQuestions,
                              Map<Integer, Question> questionNumberMap) {
        try {
            String detailsUrl = resultsUrl.endsWith("/")
                    ? resultsUrl + "details/"
                    : resultsUrl + "/details/";
            log.debug("Navigate sang details: {}", detailsUrl);
            page.navigate(detailsUrl, CrawlerUtils.domLoaded());

            try {
                page.locator("text=Đáp án đúng").first()
                        .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10_000));
            } catch (Exception e) {
                log.debug("Không thấy 'Đáp án đúng', thử scrape từ results thường");
                page.navigate(resultsUrl, CrawlerUtils.domLoaded());
            }

            List<Question> questions = (orderedQuestions != null)
                    ? orderedQuestions
                    : questionRepository.findByPassage_Section_IdOrderByCreatedAtAsc(sectionId);

            List<Answer> candidates = sectionId != null
                    ? answerRepository.findByQuestion_Passage_Section_Id(sectionId)
                    : answerRepository.findAll();

            // Strategy 0: parse "Đáp án đúng:" từ details page
            int updated = scrapeFromDetailsPage(page, questionNumberMap, questions, candidates);
            if (updated > 0) { log.debug("Details page: {} đáp án", updated); return updated; }

            // Strategy 1: match theo số câu
            if (questionNumberMap != null && !questionNumberMap.isEmpty()) {
                updated = scrapeByQuestionNumber(page, questionNumberMap);
                if (updated > 0) { log.debug("Strategy 1: {} đáp án", updated); return updated; }
            }

            // Strategy 2: map theo index
            List<ElementHandle> answerItems = page.querySelectorAll(".result-answers-item");
            if (!answerItems.isEmpty()) {
                updated += scrapeByIndexMapping(answerItems, questions);
                if (updated > 0) log.debug("Strategy 2: {} đáp án", updated);
            }

            // Strategy 3: .text-answerkey class
            int s3 = scrapeByAnswerKeyClass(page, questions, candidates);
            if (s3 > 0) { updated += s3; log.debug("Strategy 3: {} đáp án", s3); }

            // Strategy 4: .text-success / .correct-answer class
            int s4 = scrapeBySuccessClass(page, candidates);
            if (s4 > 0) { updated += s4; log.debug("Strategy 4: {} đáp án", s4); }

            return updated;
        } catch (Exception ex) {
            log.warn("Lỗi scrape đáp án từ {}: {}", resultsUrl, ex.getMessage());
            return 0;
        }
    }

    // ── Strategy 0 ────────────────────────────────────────────────────────────

    private int scrapeFromDetailsPage(Page page,
                                      Map<Integer, Question> questionNumberMap,
                                      List<Question> questions,
                                      List<Answer> candidates) {
        int updated = 0;
        try {
            String js =
                    "() => {" +
                            "  var results = [];" +
                            "  var allText = Array.from(document.querySelectorAll('p, div, span'));" +
                            "  allText.forEach(function(el) {" +
                            "    var text = el.innerText ? el.innerText.trim() : '';" +
                            "    if (text.startsWith('Đáp án đúng:') || text.startsWith('Dap an dung:')) {" +
                            "      var answer = text.replace(/^Đáp án đúng:/i, '').replace(/^Dap an dung:/i, '').trim();" +
                            "      var container = el.closest('[class*=\"question\"], [class*=\"answer\"], div');" +
                            "      var numEl = container ? container.querySelector('span, [class*=\"number\"]') : null;" +
                            "      var num = numEl ? numEl.innerText.replace(/[^0-9]/g,'').trim() : '';" +
                            "      results.push({ num: num, answer: answer });" +
                            "    }" +
                            "  });" +
                            "  return results;" +
                            "}";
            Object raw = page.evaluate(js);
            if (!(raw instanceof List<?> list) || list.isEmpty())
                return scrapeDetailsPageAlt(page, questionNumberMap, questions, candidates);

            log.debug("Details page found {} answer items", list.size());
            for (int i = 0; i < list.size(); i++) {
                if (!(list.get(i) instanceof Map<?, ?> map)) continue;
                String numStr  = map.get("num")    != null ? String.valueOf(map.get("num")).trim()    : "";
                String correct = map.get("answer") != null ? String.valueOf(map.get("answer")).trim() : "";
                if (!StringUtils.hasText(correct)) continue;

                Question q = null;
                if (!numStr.isBlank() && questionNumberMap != null) {
                    try { q = questionNumberMap.get(Integer.parseInt(numStr)); }
                    catch (Exception ignored) {}
                }
                if (q == null && i < questions.size()) q = questions.get(i);
                if (q != null) updated += answerApplier.applyAnswerToQuestion(q, correct);
                else           updated += answerApplier.markCorrectByContent(correct, candidates);
            }
        } catch (Exception ex) {
            log.warn("scrapeFromDetailsPage lỗi: {}", ex.getMessage());
        }
        return updated;
    }

    private int scrapeDetailsPageAlt(Page page,
                                     Map<Integer, Question> questionNumberMap,
                                     List<Question> questions,
                                     List<Answer> candidates) {
        int updated = 0;
        try {
            List<ElementHandle> answerEls = page.querySelectorAll(
                    "p:has-text('Đáp án đúng'), div:has-text('Đáp án đúng:')");
            if (answerEls.isEmpty()) {
                String js2 =
                        "() => {" +
                                "  return Array.from(document.querySelectorAll('*')).filter(function(el) {" +
                                "    return el.childElementCount === 0 && el.innerText && el.innerText.includes('Đáp án đúng:');" +
                                "  }).map(function(el, i) {" +
                                "    var text = el.innerText.trim();" +
                                "    var answer = text.replace(/.*Đáp án đúng:/,'').trim();" +
                                "    return { index: i, answer: answer };" +
                                "  });" +
                                "}";
                Object raw2 = page.evaluate(js2);
                if (!(raw2 instanceof List<?> list2)) return 0;
                for (int i = 0; i < list2.size(); i++) {
                    if (!(list2.get(i) instanceof Map<?, ?> map)) continue;
                    String correct = map.get("answer") != null
                            ? String.valueOf(map.get("answer")).trim() : "";
                    if (!StringUtils.hasText(correct)) continue;
                    Question q = (i < questions.size()) ? questions.get(i) : null;
                    if (q != null) updated += answerApplier.applyAnswerToQuestion(q, correct);
                    else           updated += answerApplier.markCorrectByContent(correct, candidates);
                }
                return updated;
            }
            for (int i = 0; i < answerEls.size(); i++) {
                String text    = answerEls.get(i).innerText().trim();
                String correct = text.replaceAll("(?i).*Đáp án đúng:", "").trim();
                if (!StringUtils.hasText(correct)) continue;
                Question q = (i < questions.size()) ? questions.get(i) : null;
                if (q != null) updated += answerApplier.applyAnswerToQuestion(q, correct);
                else           updated += answerApplier.markCorrectByContent(correct, candidates);
            }
        } catch (Exception ex) {
            log.warn("scrapeDetailsPageAlt lỗi: {}", ex.getMessage());
        }
        return updated;
    }

    // ── Strategy 1 ────────────────────────────────────────────────────────────

    private int scrapeByQuestionNumber(Page page, Map<Integer, Question> questionNumberMap) {
        int updated = 0;
        for (ElementHandle item : page.querySelectorAll(".result-answers-item")) {
            String numStr = null;
            ElementHandle numEl = item.querySelector(
                    "[data-question-number], .question-number, .q-number");
            if (numEl != null) numStr = numEl.innerText().replaceAll("[^0-9]", "").trim();
            if (numStr == null || numStr.isBlank()) {
                Matcher m = QUESTION_NUMBER_PATTERN.matcher(item.innerText());
                if (m.find()) numStr = m.group(1);
            }
            if (numStr == null || numStr.isBlank()) continue;
            int num; try { num = Integer.parseInt(numStr); } catch (Exception e) { continue; }
            Question q = questionNumberMap.get(num);
            if (q == null) continue;
            ElementHandle keyNode = item.querySelector(".text-answerkey");
            if (keyNode == null) continue;
            String correct = keyNode.innerText().trim();
            if (!StringUtils.hasText(correct)) continue;
            updated += answerApplier.applyAnswerToQuestion(q, correct);
        }
        return updated;
    }

    // ── Strategy 2 ────────────────────────────────────────────────────────────

    private int scrapeByIndexMapping(List<ElementHandle> answerItems,
                                     List<Question> questions) {
        int updated = 0;
        for (int i = 0; i < answerItems.size(); i++) {
            ElementHandle keyNode = answerItems.get(i).querySelector(".text-answerkey");
            if (keyNode == null) continue;
            String correct = keyNode.innerText().trim();
            if (!StringUtils.hasText(correct)) continue;
            if (i >= questions.size() || questions.get(i) == null) continue;
            updated += answerApplier.applyAnswerToQuestion(questions.get(i), correct);
        }
        return updated;
    }

    // ── Strategy 3 ────────────────────────────────────────────────────────────

    private int scrapeByAnswerKeyClass(Page page,
                                       List<Question> questions,
                                       List<Answer> candidates) {
        int updated = 0;
        for (ElementHandle node : page.querySelectorAll(".text-answerkey")) {
            String answer = node.innerText().trim();
            if (!StringUtils.hasText(answer)) continue;
            for (String part : answer.split("\\[OR\\]"))
                updated += answerApplier.markCorrectByContent(part.trim(), candidates);
        }
        return updated;
    }

    // ── Strategy 4 ────────────────────────────────────────────────────────────

    private int scrapeBySuccessClass(Page page, List<Answer> candidates) {
        int updated = 0;
        for (ElementHandle node : page.querySelectorAll(
                ".text-success, .correct-answer, [class*='correct']")) {
            String text = node.innerText().trim();
            if (StringUtils.hasText(text))
                updated += answerApplier.markCorrectByContent(text, candidates);
        }
        return updated;
    }
}