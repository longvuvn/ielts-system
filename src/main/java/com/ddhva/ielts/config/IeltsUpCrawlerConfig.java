package com.ddhva.ielts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "scheduler.exam")
@Getter
@Setter
public class IeltsUpCrawlerConfig {
    private boolean enabled;
    private String cron;
    private String baseUrl;
    private int listeningFrom;
    private int listeningTo;
    private int readingFrom;
    private int readingTo;
}