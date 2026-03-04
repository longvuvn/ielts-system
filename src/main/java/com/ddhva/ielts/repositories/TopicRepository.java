package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopicRepository extends JpaRepository <Topic, UUID>{
}
