package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.exam.res.CrawledExamDto;

public interface ListeningParserService {
    CrawledExamDto parse(int testNumber);
}