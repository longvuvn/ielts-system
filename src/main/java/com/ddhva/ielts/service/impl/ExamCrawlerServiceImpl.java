package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.enums.*;
import com.ddhva.ielts.model.*;
import com.ddhva.ielts.repositories.*;
import com.ddhva.ielts.service.ExamCrawlerService;
import com.ddhva.ielts.service.crawler.*;
import com.ddhva.ielts.service.crawler.questions.AnswerKeyScraper;
import com.ddhva.ielts.util.CrawlerUtils;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamCrawlerServiceImpl implements ExamCrawlerService {

    private static final int MAX_RETRY = 3;
    private static final String QUESTION_WRAPPER_SELECTOR =
            ".question-wrapper, .question-item, [data-question-id], [id^=question-]";
    private static final Pattern MINUTES_PATTERN =
            Pattern.compile("(\\d+)\\s*phút", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUESTIONS_PATTERN =
            Pattern.compile("(\\d+)\\s*câu hỏi", Pattern.CASE_INSENSITIVE);

    private final AtomicBoolean acceptDialogEnabled = new AtomicBoolean(false);


    private final PlaywrightFactory playwrightFactory;
    private final BrowserSessionManager sessionManager;
    private final ExamPageNavigator navigator;
    private final ExamContentExtractor extractor;
    private final AnswerKeyScraper answerKeyScraper;
    private final ExamRepository examRepository;
    private final SectionRepository sectionRepository;
    private final PassageRepository passageRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;


    @Override
    @Transactional
    public void crawlAndSave(Integer limit) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwrightFactory.launchBrowser(playwright)) {

            BrowserContext ctx = sessionManager.createAuthenticatedContext(
                    browser, acceptDialogEnabled);
            Page page = ctx.newPage();

            Set<String> examLinks = navigator.collectAllExamLinks(page);
            log.info("Tìm thấy {} đề thi", examLinks.size());

            int inserted = 0, skipped = 0, failed = 0;
            for (String examUrl : new ArrayList<>(examLinks)) {
                if (limit != null && limit > 0 && inserted >= limit) {
                    log.info("Đã crawl đủ {} đề mới, dừng lại", limit);
                    break;
                }
                int result = crawlSingleExamWithRetry(page, ctx, examUrl);
                if      (result > 0) inserted++;
                else if (result == 0) skipped++;
                else                  failed++;

                if (result > 0 && (limit == null || inserted < limit)) {
                    log.info("Nghỉ giữa đề [{} inserted]...", inserted);
                    CrawlerUtils.humanDelay(10_000, 10_000);
                }
            }
            log.info("Hoàn tất crawlAndSave. inserted={}, skipped={}, failed={}",
                    inserted, skipped, failed);
        }
    }

    @Override
    @Transactional
    public void crawlAndUpdateAnswerKey(String resultsUrl) {
        if (!StringUtils.hasText(resultsUrl))
            throw new IllegalArgumentException("resultsUrl không được để trống");
        try (Playwright playwright = Playwright.create();
             Browser browser = playwrightFactory.launchBrowser(playwright)) {
            BrowserContext ctx = sessionManager.createAuthenticatedContext(
                    browser, acceptDialogEnabled);
            Page page = ctx.newPage();
            answerKeyScraper.scrapeAndApply(page, resultsUrl, null, null, null);
        }
    }

    @Override
    @Transactional
    public void crawlAndUpdateAnswerKeysForExams(Integer limit) {
        List<Exam> exams = examRepository.findAll();
        if (limit != null && limit > 0)
            exams = exams.stream().limit(limit).toList();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwrightFactory.launchBrowser(playwright)) {
            BrowserContext ctx = sessionManager.createAuthenticatedContext(
                    browser, acceptDialogEnabled);
            Page page = ctx.newPage();

            int totalUpdated = 0, totalFailed = 0;
            for (Exam exam : exams) {
                try {
                    totalUpdated += recrawlAnswerKeysForExam(page, ctx, exam);
                } catch (Exception ex) {
                    log.warn("Lỗi re-crawl đề '{}': {}", exam.getTitle(), ex.getMessage());
                    totalFailed++;
                }
            }
            log.info("crawlAndUpdateAnswerKeysForExams xong. updated={}, failed={}",
                    totalUpdated, totalFailed);
        }
    }


    // Single exam crawl
    private int crawlSingleExamWithRetry(Page page, BrowserContext ctx, String examUrl) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                return crawlSingleExam(page, ctx, examUrl) ? 1 : 0;
            } catch (Exception ex) {
                log.warn("[Lần {}/{}] Lỗi crawl đề {}: {}",
                        attempt, MAX_RETRY, examUrl, ex.getMessage());
                if (attempt == MAX_RETRY) {
                    log.error("Bỏ qua đề {} sau {} lần thất bại", examUrl, MAX_RETRY);
                    return -1;
                }
                try { Thread.sleep(2_000L * attempt); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); return -1; }
            }
        }
        return -1;
    }

    private boolean crawlSingleExam(Page page, BrowserContext ctx, String examUrl) {
        page.navigate(examUrl, CrawlerUtils.domLoaded());
        sessionManager.ensureLoggedIn(page, ctx, acceptDialogEnabled);

        String title = CrawlerUtils.innerText(page, "h1").trim();
        if (title.isBlank()) {
            log.warn("Không lấy được tiêu đề tại {}", examUrl);
            return false;
        }
        if (examRepository.findByTitle(title).isPresent()) {
            log.info("Skip '{}' — title đã tồn tại trong DB", title);
            return false;
        }

        String metaText = CrawlerUtils.innerText(
                page, ".test-info-wrapper, .test-meta, .contentblock");
        int minutes       = CrawlerUtils.extractInt(MINUTES_PATTERN, metaText, 0);
        int questionCount = CrawlerUtils.extractInt(QUESTIONS_PATTERN, metaText, 0);

        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setStatus(ExamStatus.ACTIVE);
        exam.setSource_url(examUrl);
        exam.setMax_score(questionCount > 0
                ? BigDecimal.valueOf(questionCount) : BigDecimal.ZERO);
        exam.setDuration(Instant.EPOCH.plus(Duration.ofMinutes(Math.max(minutes, 0L))));
        exam = examRepository.saveAndFlush(exam);

        List<String> partValues = navigator.getPartValues(page);
        if (partValues.isEmpty()) {
            log.warn("Không tìm thấy section nào cho đề '{}' tại {}", title, examUrl);
            return false;
        }
        log.info("Đề '{}' — {} sections: {}", title, partValues.size(), partValues);

        int totalNewQuestions = 0;
        for (int si = 0; si < partValues.size(); si++) {
            String partValue    = partValues.get(si);
            String sectionTitle = "Section " + (si + 1);
            final Exam finalExam = exam;

            Section section = sectionRepository
                    .findByExam_IdAndTitle(finalExam.getId(), sectionTitle)
                    .orElseGet(Section::new);
            section.setExam(finalExam);
            section.setSection_number(si + 1);
            section.setTitle(sectionTitle);
            if (minutes > 0 && questionCount > 0) {
                long mins = Math.max(1L, minutes / partValues.size());
                section.setTime_limit(Instant.EPOCH.plus(Duration.ofMinutes(mins)));
            }
            section = sectionRepository.saveAndFlush(section);

            try {
                int newQ = crawlOnePart(page, ctx, examUrl, partValue, section);
                totalNewQuestions += newQ;
                log.info("[{}/{}] Section '{}' (part={}): {} câu mới",
                        si + 1, partValues.size(), sectionTitle, partValue, newQ);
            } catch (Exception ex) {
                log.warn("Lỗi crawl section '{}' (part={}): {}",
                        sectionTitle, partValue, ex.getMessage());
            }

            if (si < partValues.size() - 1) {
                if ((si + 1) % 2 == 0) {
                    log.info("Nghỉ 15s sau {} section...", si + 1);
                    CrawlerUtils.humanDelay(15_000, 15_000);
                } else {
                    CrawlerUtils.humanDelay(20_000, 20_000);
                }
            }
        }

        log.info("Đề '{}' — tổng {} câu mới", title, totalNewQuestions);
        return totalNewQuestions > 0;
    }


    // Crawl one part (section)
    private int crawlOnePart(Page page, BrowserContext ctx,
                             String examUrl, String partValue, Section section) {
        page.navigate(examUrl, CrawlerUtils.domLoaded());
        sessionManager.ensureLoggedIn(page, ctx, acceptDialogEnabled);
        page.locator("input[name=part], form[action*='practice']")
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(10_000));

        log.info("Giả lập đọc đề cho part={}...", partValue);
        CrawlerUtils.humanDelay(30_000, 60_000);

        navigator.checkOnlyOnePart(page, partValue);

        String practiceUrl = navigator.enterPractice(page, examUrl);
        if (practiceUrl == null) {
            practiceUrl = navigator.buildDirectPracticeUrl(examUrl, partValue);
            log.info("Fallback navigate trực tiếp: {}", practiceUrl);
            page.navigate(practiceUrl, CrawlerUtils.domLoaded());
            sessionManager.ensureLoggedIn(page, ctx, acceptDialogEnabled);

            String landedUrl = page.url();
            if (!landedUrl.contains("/practice/") && !landedUrl.contains("part=" + partValue)) {
                log.warn("Cloudflare block part={} — chờ 30s rồi thử lại", partValue);
                CrawlerUtils.humanDelay(30_000, 40_000);

                page.navigate(examUrl, CrawlerUtils.domLoaded());
                navigator.checkOnlyOnePart(page, partValue);
                practiceUrl = navigator.enterPractice(page, examUrl);

                if (practiceUrl == null) {
                    practiceUrl = navigator.buildDirectPracticeUrl(examUrl, partValue);
                    page.navigate(practiceUrl, CrawlerUtils.domLoaded());
                    landedUrl = page.url();
                    if (!landedUrl.contains("/practice/")
                            && !landedUrl.contains("part=" + partValue)) {
                        log.warn("Vẫn bị block sau retry part={}, bỏ qua", partValue);
                        return 0;
                    }
                }
                practiceUrl = page.url();
            } else {
                practiceUrl = landedUrl;
            }
        }

        if (practiceUrl.contains("__cf_chl_rt_tk")) {
            log.warn("Cloudflare challenge cho part={} — chờ 15s", partValue);
            CrawlerUtils.humanDelay(15_000, 20_000);
        }

        // Audio
        String audioUrl = extractor.extractAudioUrl(page);
        if (StringUtils.hasText(audioUrl)) {
            section.setAudio_url(audioUrl);
            sectionRepository.save(section);
            log.info("Section '{}' audio: {}", section.getTitle(), audioUrl);
        }

        // Wait for questions
        try {
            page.locator(QUESTION_WRAPPER_SELECTOR)
                    .first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(20_000));
        } catch (PlaywrightException e) {
            log.warn("Không tìm thấy câu hỏi cho part={} tại {}", partValue, page.url());
            return 0;
        }

        // Passage
        Passage passage = new Passage();
        passage.setSection(section);
        passage.setPassage_number(
                (int) passageRepository.countBySection_Id(section.getId()) + 1);
        passage.setContent_html(extractor.extractPassageHtml(page));
        passage.setInstruction(extractor.extractInstruction(page));
        passage = passageRepository.saveAndFlush(passage);
        log.debug("Đã tạo Passage #{} cho section '{}'",
                passage.getPassage_number(), section.getTitle());

        // Parse questions
        Locator wrappers = page.locator(QUESTION_WRAPPER_SELECTOR);
        if (wrappers.count() == 0) {
            log.warn("0 wrapper câu hỏi cho part={}", partValue);
            return 0;
        }

        List<Question>         orderedQuestions = new ArrayList<>();
        Map<Integer, Question> questionNumberMap = new LinkedHashMap<>();
        Set<String>            existingContents  = new HashSet<>(
                questionRepository.findContentsByPassage_Section_Id(section.getId()));
        final Passage finalPassage = passage;

        for (int i = 0; i < wrappers.count(); i++) {
            Locator wrapper = wrappers.nth(i);
            String content  = extractor.extractQuestionContent(wrapper);
            if (content.isBlank()) { orderedQuestions.add(null); continue; }

            Integer qNum = extractor.extractQuestionNumber(wrapper, i + 1);

            if (existingContents.contains(content)) {
                Question existing = questionRepository
                        .findByPassage_Section_IdAndContent(section.getId(), content)
                        .orElse(null);
                orderedQuestions.add(existing);
                if (existing != null && qNum != null)
                    questionNumberMap.put(qNum, existing);
                continue;
            }

            Question q = new Question();
            q.setPassage(finalPassage);
            q.setContent(content);
            q.setImage_url(extractor.extractQuestionImageUrl(wrapper));
            q.setStatus(QuestionStatus.ACTIVE);
            q.setType(extractor.detectQuestionType(wrapper));
            q.setScore(BigDecimal.ONE);
            q.setLevel(LevelType.Medium);
            q = questionRepository.save(q);
            existingContents.add(content);
            orderedQuestions.add(q);
            if (qNum != null) questionNumberMap.put(qNum, q);
            saveOptions(wrapper, q);
        }

        log.info("Giả lập làm bài 10 phút cho part={}...", partValue);
        CrawlerUtils.humanDelay(200_000, 200_000);

        Object filled = page.evaluate(CrawlerUtils.buildFillAllInputsJs());
        log.debug("Filled {} inputs cho part={}", filled, partValue);

        String resultsUrl = navigator.submitPractice(page, page.url(), acceptDialogEnabled);
        if (!StringUtils.hasText(resultsUrl)) {
            log.warn("Không lấy được results URL cho part={}", partValue);
            return CrawlerUtils.countNonNull(orderedQuestions);
        }

        int updated = answerKeyScraper.scrapeAndApply(
                page, resultsUrl, section.getId(), orderedQuestions, questionNumberMap);
        log.debug("part={}: {} đáp án cập nhật", partValue, updated);

        return CrawlerUtils.countNonNull(orderedQuestions);
    }


    // Re-crawl answer keys
    private int recrawlAnswerKeysForExam(Page page, BrowserContext ctx, Exam exam) {
        if (!StringUtils.hasText(exam.getSource_url())) {
            log.warn("Đề '{}' không có source_url, bỏ qua", exam.getTitle());
            return 0;
        }

        page.navigate(exam.getSource_url(), CrawlerUtils.domLoaded());
        sessionManager.ensureLoggedIn(page, ctx, acceptDialogEnabled);

        List<String> partValues = navigator.getPartValues(page);
        if (partValues.isEmpty()) {
            log.warn("Không tìm thấy part nào cho đề '{}', bỏ qua", exam.getTitle());
            return 0;
        }

        List<Section> dbSections = exam.getSections() != null ? exam.getSections() : List.of();
        int totalUpdated = 0;

        for (int i = 0; i < partValues.size(); i++) {
            String partValue = partValues.get(i);
            UUID sectionId = null;
            if (i < dbSections.size()) {
                sectionId = dbSections.get(i).getId();
                long qCount  = questionRepository.countByPassage_Section_Id(sectionId);
                long correct = answerRepository
                        .countDistinctQuestionsByPassage_Section_IdAndIs_correctTrue(sectionId);
                if (qCount > 0 && correct >= qCount) {
                    log.debug("Section {} đã đủ đáp án, skip", i + 1);
                    continue;
                }
            }

            try {
                page.navigate(exam.getSource_url(), CrawlerUtils.domLoaded());
                sessionManager.ensureLoggedIn(page, ctx, acceptDialogEnabled);
                navigator.checkOnlyOnePart(page, partValue);

                String practiceUrl = navigator.enterPractice(page, exam.getSource_url());
                if (practiceUrl == null) {
                    practiceUrl = navigator.buildDirectPracticeUrl(
                            exam.getSource_url(), partValue);
                    page.navigate(practiceUrl, CrawlerUtils.domLoaded());
                }

                Object filled = page.evaluate(CrawlerUtils.buildFillAllInputsJs());
                log.debug("Re-crawl part={}: filled {}", partValue, filled);

                String resultsUrl = navigator.submitPractice(
                        page, page.url(), acceptDialogEnabled);
                if (StringUtils.hasText(resultsUrl)) {
                    int updated = answerKeyScraper.scrapeAndApply(
                            page, resultsUrl, sectionId, null, null);
                    log.info("Re-crawl section {} (part={}): updated={}",
                            i + 1, partValue, updated);
                    totalUpdated += updated;
                }

                if (i < partValues.size() - 1)
                    CrawlerUtils.humanDelay(2_000, 4_000);
            } catch (Exception ex) {
                log.warn("Lỗi re-crawl section {} (part={}): {}",
                        i + 1, partValue, ex.getMessage());
            }
        }
        return totalUpdated;
    }

    // Save answer options
    private void saveOptions(Locator wrapper, Question question) {
        Set<String> existing = new HashSet<>(
                answerRepository.findContentsByQuestion_Id(question.getId()));

        Locator labels = wrapper.locator("label");
        for (int i = 0; i < labels.count(); i++) {
            String text = CrawlerUtils.sanitize(labels.nth(i).innerText());
            if (text.isBlank() || existing.contains(text)) continue;
            existing.add(text);
            answerRepository.save(buildAnswer(question, text));
        }
        Locator options = wrapper.locator("option");
        for (int i = 0; i < options.count(); i++) {
            String text = CrawlerUtils.sanitize(options.nth(i).innerText());
            if (text.isBlank() || "-- Chọn --".equalsIgnoreCase(text)
                    || existing.contains(text)) continue;
            existing.add(text);
            answerRepository.save(buildAnswer(question, text));
        }
        if (QuestionType.FILL_IN_BLANK.equals(question.getType())
                && !existing.contains("__FILL_IN_BLANK__")) {
            answerRepository.save(buildAnswer(question, "__FILL_IN_BLANK__"));
        }
    }

    private Answer buildAnswer(Question question, String content) {
        Answer a = new Answer();
        a.setQuestion(question);
        a.setContent(content);
        a.setStatus(AnswerStatus.ACTIVE);
        a.setIs_correct(null);
        return a;
    }
}