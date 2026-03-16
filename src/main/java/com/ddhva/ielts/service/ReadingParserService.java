package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.exam.res.CrawledExamDto;

public interface ReadingParserService {
    CrawledExamDto parse(String type, int testNumber, int section);
}