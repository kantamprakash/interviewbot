package com.ai.interview.repository;

import com.ai.interview.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Question> findByTopicAndIsActiveTrueOrderByDifficulty(String topic);
    List<Question> findByDifficultyAndIsActiveTrueOrderByCreatedAtDesc(String difficulty);
    List<Question> findByTopicAndDifficultyAndIsActiveTrue(String topic, String difficulty);
}
