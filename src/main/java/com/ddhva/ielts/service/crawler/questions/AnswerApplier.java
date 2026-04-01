package com.ddhva.ielts.service.crawler.questions;

import com.ddhva.ielts.enums.AnswerStatus;
import com.ddhva.ielts.model.Answer;
import com.ddhva.ielts.model.Question;
import com.ddhva.ielts.repositories.AnswerRepository;
import com.ddhva.ielts.util.CrawlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnswerApplier {

    private final AnswerRepository answerRepository;

    public int applyAnswerToQuestion(Question q, String correctAnswer) {
        List<Answer> answers = answerRepository.findByQuestion_Id(q.getId());
        if (answers.stream().anyMatch(a -> Boolean.TRUE.equals(a.getIs_correct()))) return 0;

        String[] parts = correctAnswer.split("\\[OR\\]");
        for (String part : parts) {
            String partClean = CrawlerUtils.sanitize(part.trim());

            for (Answer a : answers) {
                String aContent = CrawlerUtils.sanitize(a.getContent());

                // Match trực tiếp
                if (CrawlerUtils.isMatch(aContent, partClean)) {
                    return markCorrect(a);
                }

                // Match theo chữ cái (A/B/C/D)
                if (partClean.matches("^[A-Da-d]$")) {
                    int idx = partClean.toLowerCase().charAt(0) - 'a';
                    List<Answer> sorted = new ArrayList<>(answers);
                    if (idx < sorted.size()) {
                        return markCorrect(sorted.get(idx));
                    }
                }

                // Match sau khi strip prefix (A. / 1) / ...)
                String aStripped = aContent.replaceAll("^[A-Da-d1-4][.)\\s]+", "").trim();
                String pStripped = partClean.replaceAll("^[A-Da-d1-4][.)\\s]+", "").trim();
                if (CrawlerUtils.isMatch(aStripped, pStripped)) {
                    return markCorrect(a);
                }
            }
        }

        // Không match answer nào → tạo/update placeholder fill-in-blank
        String primaryAnswer = parts[0].trim();
        Answer placeholder = answers.stream()
                .filter(a -> "__FILL_IN_BLANK__".equals(a.getContent()))
                .findFirst().orElse(null);

        if (placeholder != null) {
            placeholder.setContent(primaryAnswer);
            placeholder.setIs_correct(true);
            answerRepository.save(placeholder);
        } else {
            Answer newAns = new Answer();
            newAns.setQuestion(q);
            newAns.setContent(primaryAnswer);
            newAns.setIs_correct(true);
            newAns.setStatus(AnswerStatus.ACTIVE);
            answerRepository.save(newAns);
        }
        return 1;
    }

    /**
     * Tìm trong danh sách candidates và đánh dấu đúng theo nội dung.
     */
    public int markCorrectByContent(String correctText, List<Answer> candidates) {
        if (!StringUtils.hasText(correctText)) return 0;
        int updated = 0;
        for (Answer a : candidates) {
            if (Boolean.TRUE.equals(a.getIs_correct())) continue;
            if (CrawlerUtils.isMatch(
                    CrawlerUtils.sanitize(a.getContent()),
                    CrawlerUtils.sanitize(correctText))) {
                a.setIs_correct(true);
                answerRepository.save(a);
                updated++;
            }
        }
        return updated;
    }

    // ── private ───────────────────────────────────────────────────────────────

    private int markCorrect(Answer a) {
        a.setIs_correct(true);
        answerRepository.save(a);
        return 1;
    }
}