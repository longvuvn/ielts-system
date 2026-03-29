package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.exam.res.ExamResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.section.res.SectionResponse;
import com.ddhva.ielts.model.Exam;
import com.ddhva.ielts.model.Section;
import com.ddhva.ielts.repositories.ExamRepository;
import com.ddhva.ielts.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ModelMapper modelMapper;

    @Override
    public Pagination<ExamResponse> getAllExams(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exam> exams = examRepository.findAll(pageable);
        List<ExamResponse> responses = exams.stream()
                .map(exam -> modelMapper.map(exam, ExamResponse.class))
                .toList();
        Pagination<ExamResponse> pagination = new Pagination<>();
        pagination.setPage(page);
        pagination.setSize(size);
        pagination.setTotalElements(exams.getTotalElements());
        pagination.setTotalPages(exams.getTotalPages());
        pagination.setContent(responses);
        return pagination;
    }

    @Override
    public List<SectionResponse> getSectionByExamId(String examId) {
        UUID examUUID = UUID.fromString(examId);
        List<Section> section = examRepository.findByExam_Id(examUUID);
        return section.stream()
                .map(s -> {
                    SectionResponse res = modelMapper.map(s, SectionResponse.class);
                    if(!res.getTime_limit().isEmpty() && !res.getSection_number().isEmpty() && !res.getTime_limit().equals("null")){
                        res.setTime_limit(s.getTime_limit().toString());
                        res.setSection_number(s.getSection_number().toString());
                        res.setAudio_url(s.getAudio_url());
                    }
                    return res;
                })
                .collect(Collectors.toList());
    }
}
