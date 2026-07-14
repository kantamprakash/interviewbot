package com.ai.interview.repository;

import com.ai.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByStatusOrderByCreatedAtDesc(String status);
    List<InterviewSession> findAllByOrderByCreatedAtDesc();
    List<InterviewSession> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}
