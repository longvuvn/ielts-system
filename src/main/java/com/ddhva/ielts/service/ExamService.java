package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.exam.req.ExamRequest;
import com.ddhva.ielts.dto.exam.res.ExamResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.section.res.SectionResponse;

import java.util.List;


public interface ExamService {
    Pagination<ExamResponse> getAllExams(int page, int size);
    List<SectionResponse> getSectionByExamId(String examId);
    ExamResponse getExamById(String examId);
    ExamResponse updateExam(String examId, ExamRequest request);
    void deleteExam(String examId);
}
