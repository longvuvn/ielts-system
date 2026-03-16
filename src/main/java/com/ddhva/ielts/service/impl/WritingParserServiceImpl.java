package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.dto.exam.res.CrawledExamDto;
import com.ddhva.ielts.service.WritingParserService;
import com.ddhva.ielts.util.JsoupFetchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WritingParserServiceImpl implements WritingParserService {

    private final IeltsUpCrawlerConfig config;
    private final JsoupFetchUtil fetchUtil;

    @Override
    public CrawledExamDto parse(String slug) {
        String url = config.getBaseUrl() + "/writing/" + slug + ".html";
        Document doc = fetchUtil.fetch(url);
        if (doc == null) return null;

        String title = doc.select("h1").text().trim();
        StringBuilder task = new StringBuilder();
        StringBuilder model = new StringBuilder();
        boolean reachedModel = false;

        for (Element el : doc.select("p, blockquote")) {
            String text = el.text().trim();
            if (text.isEmpty() || text.length() < 10) continue;
            if (text.toLowerCase().contains("model answer")) {
                reachedModel = true;
                continue;
            }
            if (!reachedModel) {
                if (text.contains("Write at least") || text.contains("You should spend")
                        || text.contains("Some people") || text.contains("Describe")
                        || text.contains("Do you agree")) {
                    task.append(text).append("\n");
                }
            } else {
                model.append(text).append("\n");
            }
        }

        log.info("[WRITING] '{}' parsed", slug);

        return CrawledExamDto.builder()
                .title(title)
                .sections(List.of(CrawledExamDto.CrawledSectionDto.builder()
                        .title("Writing")
                        .skillType("WRITING")
                        .sectionNumber(1)
                        .questions(List.of(CrawledExamDto.CrawledQuestionDto.builder()
                                .content(task.toString().trim())
                                .questionType("WRITING")
                                .answers(List.of(CrawledExamDto.CrawledAnswerDto.builder()
                                        .content(model.toString().trim())
                                        .isCorrect(true)
                                        .build()))
                                .build()))
                        .build()))
                .build();
    }
}