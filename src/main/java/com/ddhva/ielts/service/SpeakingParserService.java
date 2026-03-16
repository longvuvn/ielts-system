package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.exam.res.CrawledExamDto;

public interface SpeakingParserService {
    CrawledExamDto parse(String slug);
}