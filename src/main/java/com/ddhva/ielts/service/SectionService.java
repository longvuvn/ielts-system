package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.passage.PassageResponse;

import java.util.List;

public interface SectionService {
    List<PassageResponse> getPassageBySectionId(String sectionId);
}
