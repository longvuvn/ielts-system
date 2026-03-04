package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LibraryRepository extends JpaRepository<Library, UUID> {
}
