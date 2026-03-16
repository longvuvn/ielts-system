package com.ddhva.ielts.util;

import com.ddhva.ielts.config.IeltsUpCrawlerConfig;
import com.ddhva.ielts.service.ExamCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExamCrawlerScheduler {

    private final ExamCrawlerService examCrawlerService;
    private final IeltsUpCrawlerConfig config;

    @Scheduled(cron = "${scheduler.exam.cron}")
    public void run() {
        if (!config.isEnabled()) {
            log.info("[SCHEDULER] Exam crawl disabled.");
            return;
        }
        log.info("[SCHEDULER] Triggering exam crawl...");
        examCrawlerService.crawlAndSave();
    }
}