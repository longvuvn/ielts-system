package com.ddhva.ielts.repositories;

import com.ddhva.ielts.enums.VocabularyStatus;
import com.ddhva.ielts.model.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VocabularyRepository extends JpaRepository <Vocabulary, UUID>{
    @Query("""
    SELECT v
    FROM Vocabulary v
    WHERE v.topic.id = :topicId AND v.status = "ACTIVE"
""")
    Page<Vocabulary> findByTopic_Id(UUID topicId, Pageable pageable);

    @Query("""
    SELECT v
    FROM Vocabulary v
    WHERE LOWER(v.word) LIKE LOWER(CONCAT('%', :word, '%'))
    AND v.status = "ACTIVE"
""")
    Optional<Page<Vocabulary>> searchWord(@Param("word") String word, Pageable pageable);

    @Query("""
    SELECT v
    FROM Vocabulary v
    WHERE LOWER(v.word) = LOWER(:word)
    AND v.status = "ACTIVE"
""")
    Optional<Vocabulary> findByWord(String word);

    Optional<Vocabulary> findByWordIgnoreCaseAndStatus(String word, VocabularyStatus status);
}