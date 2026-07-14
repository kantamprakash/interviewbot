package com.ai.interview.repository;

import com.ai.interview.entity.Answer;
import com.ai.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findBySession(InterviewSession session);
    Optional<Answer> findBySessionAndQuestionId(InterviewSession session, String questionId);
}
