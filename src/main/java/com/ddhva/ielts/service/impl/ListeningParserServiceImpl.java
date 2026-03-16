package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.dto.exam.res.CrawledExamDto;
import com.ddhva.ielts.service.ListeningParserService;
import com.ddhva.ielts.util.JsoupFetchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListeningParserServiceImpl implements ListeningParserService {

    private final IeltsUpCrawlerConfig config;
    private final JsoupFetchUtil fetchUtil;

    @Override
    public CrawledExamDto parse(int testNumber) {
        List<CrawledExamDto.CrawledSectionDto> sections = new ArrayList<>();

        for (int s = 1; s <= 4; s++) {
            String url = config.getBaseUrl()
                    + "/listening/ielts-listening-sample-" + testNumber + "." + s + ".html";
            Document doc = fetchUtil.fetch(url);
            if (doc == null) continue;

            List<String> answers = extractAnswers(doc);
            List<CrawledExamDto.CrawledQuestionDto> questions = extractQuestions(doc, answers);

            sections.add(CrawledExamDto.CrawledSectionDto.builder()
                    .title("Listening Section " + s)
                    .skillType("LISTENING")
                    .sectionNumber(s)
                    .questions(questions)
                    .build());

            log.info("[LISTENING] Test {} section {} → {} questions", testNumber, s, questions.size());
            fetchUtil.sleep(1500);
        }

        return CrawledExamDto.builder()
                .title("IELTS Listening Test " + testNumber)
                .sections(sections)
                .build();
    }

    private List<CrawledExamDto.CrawledQuestionDto> extractQuestions(Document doc, List<String> correctAnswers) {
        List<CrawledExamDto.CrawledQuestionDto> questions = new ArrayList<>();
        int qIdx = 0;

        for (Element ol : doc.select("ol")) {
            for (Element li : ol.select("> li")) {
                String content = li.ownText().trim();
                if (content.isEmpty()) content = li.text().trim();
                if (content.isEmpty()) continue;

                String correct = qIdx < correctAnswers.size() ? correctAnswers.get(qIdx) : "";
                Elements subOptions = li.select("ol > li, ul > li");
                List<CrawledExamDto.CrawledAnswerDto> answers = new ArrayList<>();
                String qType;

                if (!subOptions.isEmpty()) {
                    qType = "MULTIPLE_CHOICE";
                    String[] labels = {"A", "B", "C", "D", "E", "F"};
                    for (int i = 0; i < subOptions.size(); i++) {
                        String label = i < labels.length ? labels[i] : String.valueOf(i + 1);
                        answers.add(CrawledExamDto.CrawledAnswerDto.builder()
                                .content(label + ". " + subOptions.get(i).text().trim())
                                .isCorrect(label.equalsIgnoreCase(correct))
                                .build());
                    }
                } else {
                    qType = "FILL_IN_BLANK";
                    answers.add(CrawledExamDto.CrawledAnswerDto.builder()
                            .content(correct)
                            .isCorrect(true)
                            .build());
                }

                questions.add(CrawledExamDto.CrawledQuestionDto.builder()
                        .content(content)
                        .questionType(qType)
                        .answers(answers)
                        .build());
                qIdx++;
            }
        }
        return questions;
    }

    private List<String> extractAnswers(Document doc) {
        List<String> answers = new ArrayList<>();
        boolean inBlock = false;
        for (Element el : doc.getAllElements()) {
            if (el.ownText().trim().equalsIgnoreCase("ANSWERS")) {
                inBlock = true;
                continue;
            }
            if (!inBlock) continue;
            for (String line : el.text().split("\n")) {
                line = line.trim();
                if (line.matches("^\\d+\\.\\s*.+"))
                    answers.add(line.replaceAll("^\\d+\\.\\s*", "").trim());
            }
            if (answers.size() >= 10) break;
        }
        return answers;
    }
}