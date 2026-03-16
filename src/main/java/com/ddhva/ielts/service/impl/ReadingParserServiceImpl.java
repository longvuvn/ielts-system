package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.dto.exam.res.CrawledExamDto;
import com.ddhva.ielts.service.ReadingParserService;
import com.ddhva.ielts.util.JsoupFetchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadingParserServiceImpl implements ReadingParserService {

    private final IeltsUpCrawlerConfig config;
    private final JsoupFetchUtil fetchUtil;

    @Override
    public CrawledExamDto parse(String type, int testNumber, int section) {
        String url = config.getBaseUrl()
                + "/reading/" + type + "-reading-sample-" + testNumber + "." + section + ".html";
        Document doc = fetchUtil.fetch(url);
        if (doc == null) return null;

        String title = doc.select("h1").text().trim();
        List<String> correctAnswers = extractAnswers(doc);
        List<CrawledExamDto.CrawledQuestionDto> questions = extractQuestions(doc, correctAnswers);

        log.info("[READING] {} test {} section {} → {} questions", type, testNumber, section, questions.size());

        return CrawledExamDto.builder()
                .title(title)
                .sections(List.of(CrawledExamDto.CrawledSectionDto.builder()
                        .title("Reading Section " + section)
                        .skillType("READING")
                        .sectionNumber(section)
                        .questions(questions)
                        .build()))
                .build();
    }

    private List<CrawledExamDto.CrawledQuestionDto> extractQuestions(Document doc, List<String> correctAnswers) {
        List<CrawledExamDto.CrawledQuestionDto> questions = new ArrayList<>();
        int qIdx = 0;

        for (Element el : doc.select("p, li")) {
            String text = el.text().trim();
            if (!text.matches("^\\d+[.)].+")) continue;

            String content = text.replaceAll("^\\d+[.)]\\s*", "").trim();
            if (content.isEmpty()) continue;

            String correct = qIdx < correctAnswers.size() ? correctAnswers.get(qIdx) : "";
            List<CrawledExamDto.CrawledAnswerDto> answers = new ArrayList<>();
            String qType;

            boolean isTrueFalse = correct.equalsIgnoreCase("true")
                    || correct.equalsIgnoreCase("false")
                    || correct.equalsIgnoreCase("not given")
                    || correct.equalsIgnoreCase("yes")
                    || correct.equalsIgnoreCase("no");

            if (isTrueFalse) {
                qType = "TRUE_FALSE";
                for (String opt : new String[]{"True", "False", "Not Given"}) {
                    answers.add(CrawledExamDto.CrawledAnswerDto.builder()
                            .content(opt)
                            .isCorrect(opt.equalsIgnoreCase(correct))
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
            if (answers.size() >= 14) break;
        }
        return answers;
    }
}