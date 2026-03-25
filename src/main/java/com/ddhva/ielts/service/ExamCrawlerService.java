package com.ddhva.ielts.service;

public interface ExamCrawlerService {
    void crawlAndSave(Integer limit);
    void crawlAndUpdateAnswerKey(String resultsUrl);
    void crawlAndUpdateAnswerKeysForExams(Integer limit);
}