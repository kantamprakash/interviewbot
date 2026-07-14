package com.ai.interview.service;

import com.ai.interview.dto.EvaluationDTO;
import com.ai.interview.entity.Evaluation;
import com.ai.interview.entity.InterviewSession;
import com.ai.interview.repository.EvaluationRepository;
import com.ai.interview.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationResultService {

    private final EvaluationRepository evaluationRepository;
    private final InterviewSessionRepository sessionRepository;

    public EvaluationDTO getEvaluation(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Evaluation evaluation = evaluationRepository.findBySession(session)
                .orElseThrow(() -> new RuntimeException("Evaluation not found for this session"));

        return mapToDTO(evaluation);
    }

    private EvaluationDTO mapToDTO(Evaluation evaluation) {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(evaluation.getId());
        dto.setSessionId(evaluation.getSession().getId());
        dto.setOverallScore(evaluation.getOverallScore());
        dto.setTotalQuestionsAttempted(evaluation.getTotalQuestionsAttempted());
        dto.setAverageScore(evaluation.getAverageScore());
        dto.setStrengths(evaluation.getStrengths());
        dto.setAreasForImprovement(evaluation.getAreasForImprovement());
        dto.setSummary(evaluation.getSummary());
        dto.setEvaluatedAt(evaluation.getEvaluatedAt());
        return dto;
    }
}
