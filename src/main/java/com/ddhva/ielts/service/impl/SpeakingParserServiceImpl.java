package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.dto.exam.res.CrawledExamDto;
import com.ddhva.ielts.service.SpeakingParserService;
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
public class SpeakingParserServiceImpl implements SpeakingParserService {

    private final IeltsUpCrawlerConfig config;
    private final JsoupFetchUtil fetchUtil;

    @Override
    public CrawledExamDto parse(String slug) {
        String url = config.getBaseUrl() + "/speaking/" + slug + ".html";
        Document doc = fetchUtil.fetch(url);
        if (doc == null) return null;

        String title = doc.select("h1").text().trim();
        List<CrawledExamDto.CrawledSectionDto> sections = new ArrayList<>();
        int partNum = 0;

        for (Element h3 : doc.select("h3")) {
            if (!h3.text().trim().toLowerCase().startsWith("part")) continue;
            partNum++;

            List<CrawledExamDto.CrawledQuestionDto> questions = new ArrayList<>();
            Element sibling = h3.nextElementSibling();

            while (sibling != null && !sibling.tagName().equalsIgnoreCase("h3")) {
                String text = sibling.text().trim();
                if (sibling.tagName().equalsIgnoreCase("p")
                        && text.endsWith("?")
                        && text.length() < 200) {
                    Element next = sibling.nextElementSibling();
                    String modelAns = (next != null) ? next.text().trim() : "";
                    questions.add(CrawledExamDto.CrawledQuestionDto.builder()
                            .content(text)
                            .questionType("SPEAKING")
                            .answers(List.of(CrawledExamDto.CrawledAnswerDto.builder()
                                    .content(modelAns)
                                    .isCorrect(true)
                                    .build()))
                            .build());
                }
                sibling = sibling.nextElementSibling();
            }

            if (!questions.isEmpty()) {
                sections.add(CrawledExamDto.CrawledSectionDto.builder()
                        .title(h3.text().trim())
                        .skillType("SPEAKING")
                        .sectionNumber(partNum)
                        .questions(questions)
                        .build());
            }
        }

        log.info("[SPEAKING] '{}' → {} parts", slug, sections.size());
        return CrawledExamDto.builder().title(title).sections(sections).build();
    }
}