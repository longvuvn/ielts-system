package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.dto.exam.res.CrawledExamDto;
import com.ddhva.ielts.service.*;
import com.ddhva.ielts.util.JsoupFetchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExamCrawlerServiceImpl implements ExamCrawlerService {

    private final IeltsUpCrawlerConfig config;
    private final ListeningParserService listeningParser;
    private final ReadingParserService readingParser;
    private final WritingParserService writingParser;
    private final SpeakingParserService speakingParser;
    private final ExamPersistenceService persistenceService;
    private final JsoupFetchUtil fetchUtil;

    private static final List<String> WRITING_SLUGS = List.of(
            "ielts-essay-sample-1", "ielts-essay-sample-2", "ielts-essay-sample-3",
            "academic-writing-sample-1", "academic-writing-sample-2"
    );

    private static final List<String> SPEAKING_SLUGS = List.of(
            "ielts-speaking-sample-1", "ielts-speaking-sample-2",
            "ielts-speaking-sample-3", "ielts-speaking-sample-4",
            "ielts-speaking-sample-5", "ielts-speaking-sample-6"
    );

    @Override
    public void crawlAndSave() {
        log.info("[CRAWLER] ===== START =====");

        crawlListening();
        crawlReading();
        crawlWriting();
        crawlSpeaking();

        log.info("[CRAWLER] ===== DONE =====");
    }

    private void crawlListening() {
        for (int i = config.getListeningFrom(); i <= config.getListeningTo(); i++) {
            CrawledExamDto dto = listeningParser.parse(i);
            if (dto != null) persistenceService.save(dto);
            fetchUtil.sleep(2000);
        }
    }

    private void crawlReading() {
        for (int i = config.getReadingFrom(); i <= config.getReadingTo(); i++) {
            List<CrawledExamDto.CrawledSectionDto> sections = new ArrayList<>();
            for (int s = 1; s <= 3; s++) {
                CrawledExamDto dto = readingParser.parse("academic", i, s);
                if (dto != null && dto.getSections() != null)
                    sections.addAll(dto.getSections());
                fetchUtil.sleep(1500);
            }
            if (!sections.isEmpty()) {
                persistenceService.save(CrawledExamDto.builder()
                        .title("IELTS Academic Reading Test " + i)
                        .sections(sections)
                        .build());
            }
        }
    }

    private void crawlWriting() {
        for (String slug : WRITING_SLUGS) {
            CrawledExamDto dto = writingParser.parse(slug);
            if (dto != null) persistenceService.save(dto);
            fetchUtil.sleep(2000);
        }
    }

    private void crawlSpeaking() {
        for (String slug : SPEAKING_SLUGS) {
            CrawledExamDto dto = speakingParser.parse(slug);
            if (dto != null) persistenceService.save(dto);
            fetchUtil.sleep(2000);
        }
    }
}