package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.passage.PassageResponse;
import com.ddhva.ielts.dto.question.res.QuestionResponse;
import com.ddhva.ielts.model.Passage;
import com.ddhva.ielts.model.Section;
import com.ddhva.ielts.repositories.SectionRepository;
import com.ddhva.ielts.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PassageResponse> getPassageBySectionId(String sectionId) {
        UUID sectionUUID = UUID.fromString(sectionId);
        Section section = sectionRepository.findById(sectionUUID)
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
        List<Passage> passages = sectionRepository.findByIdWithPassage(sectionUUID)
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
        return passages.stream()
                .map(p -> {
                    PassageResponse res = modelMapper.map(p, PassageResponse.class);
                    List<QuestionResponse> sortedQuestions = res.getQuestions().stream()
                            .sorted(Comparator.comparingInt(q -> {
                                try {
                                    return Integer.parseInt(q.getQuestion_number());
                                } catch (Exception e) {
                                    return Integer.MAX_VALUE;
                                }
                            }))
                            .map(q -> modelMapper.map(q, QuestionResponse.class))
                            .toList();

                    res.setQuestions(sortedQuestions);
                    if (section.getAudio_url() != null && !section.getAudio_url().isEmpty()) {
                        res.setAudio_url(section.getAudio_url());
                    }
                    return res;
                })
                .collect(Collectors.toList());
    }
}
