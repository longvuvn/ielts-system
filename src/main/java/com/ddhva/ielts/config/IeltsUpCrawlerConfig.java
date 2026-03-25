package com.ddhva.ielts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "crawler")
public class IeltsUpCrawlerConfig {
    private String baseUrl;
    private String testsPath;
    private Integer maxPages;
    private int listeningFrom = 0;
    private int listeningTo   = 0;
    private Integer submitWaitMinSeconds = 0;

    @Deprecated
    private String sessionCookie;

    @Deprecated
    private Long requestDelayMs;

    @Deprecated
    private Integer maxRetries;

    @Deprecated
    private Long retryBackoffMs;

    @Deprecated
    private Integer submitWaitMaxSeconds;
}