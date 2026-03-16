package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.exam.res.CrawledExamDto;

public interface ExamPersistenceService {
    void save(CrawledExamDto dto);
}
