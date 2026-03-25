package com.ddhva.ielts.config;

import com.ddhva.ielts.service.ExamCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamCrawlerScheduler {

    private final ExamCrawlerService examCrawlerService;

    @Scheduled(cron = "0 * 0 ? * 3")
    public void scheduledCrawl() {
        Thread.currentThread().setContextClassLoader(
                getClass().getClassLoader()
        );
        try {
            log.info("Starting scheduled exam crawl");
            examCrawlerService.crawlAndSave(2);
            log.info("Scheduled exam crawl finished");
        } catch (Exception ex) {
            log.error("Scheduled exam crawl failed", ex);
        }
    }
}