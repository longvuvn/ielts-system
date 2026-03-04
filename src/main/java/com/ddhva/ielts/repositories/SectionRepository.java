package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SectionRepository extends JpaRepository <Section, UUID>{
}
