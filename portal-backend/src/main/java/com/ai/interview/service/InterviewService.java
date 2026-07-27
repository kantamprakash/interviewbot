package com.ai.interview.service;

import com.ai.interview.dto.AiEvaluationResponse;
import com.ai.interview.dto.AnswerDTO;
import com.ai.interview.dto.AssignedQuestionDTO;
import com.ai.interview.dto.EvaluationDTO;
import com.ai.interview.dto.InterviewSessionDTO;
import com.ai.interview.dto.ScheduleInterviewRequest;
import com.ai.interview.dto.SubmitAnswerRequest;
import com.ai.interview.entity.Answer;
import com.ai.interview.entity.Evaluation;
import com.ai.interview.entity.InterviewSession;
import com.ai.interview.entity.Question;
import com.ai.interview.entity.User;
import com.ai.interview.repository.AnswerRepository;
import com.ai.interview.repository.EvaluationRepository;
import com.ai.interview.repository.InterviewSessionRepository;
import com.ai.interview.repository.QuestionRepository;
import com.ai.interview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final AnswerRepository answerRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationService evaluationService;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final RecordingStorageService recordingStorageService;
    private final EmailService emailService;

    public InterviewSessionDTO scheduleInterview(ScheduleInterviewRequest request, Long adminId) {
        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        List<Question> questions = questionRepository.findAllById(request.getQuestionIds());
        if (questions.size() != request.getQuestionIds().size()) {
            throw new RuntimeException("One or more selected questions do not exist");
        }

        InterviewSession session = new InterviewSession();
        session.setTitle(request.getTitle());
        session.setCandidate(candidate);
        session.setScheduledByUserId(adminId);
        session.setDueAt(request.getDueAt());
        session.setAssignedQuestions(questions);

        InterviewSession saved = sessionRepository.save(session);
        emailService.sendInterviewScheduledEmail(saved);

        return mapToDTO(saved, true);
    }

    public List<InterviewSessionDTO> getSessionsForCandidate(Long candidateId) {
        return sessionRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId)
                .stream()
                .map(s -> mapToDTO(s, false))
                .collect(Collectors.toList());
    }

    public List<InterviewSessionDTO> getAllSessionsForAdmin() {
        return sessionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(s -> mapToDTO(s, true))
                .collect(Collectors.toList());
    }

    public InterviewSessionDTO getSession(Long sessionId, Long requesterId, String requesterRole) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        boolean isAdmin = "ADMIN".equals(requesterRole);
        boolean isOwner = session.getCandidate().getId().equals(requesterId);
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Access denied");
        }

        return mapToDTO(session, isAdmin);
    }

    public AnswerDTO submitAnswer(Long sessionId, Long requesterId, SubmitAnswerRequest request) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getCandidate().getId().equals(requesterId)) {
            throw new RuntimeException("Access denied");
        }

        boolean isAssigned = session.getAssignedQuestions().stream()
                .anyMatch(q -> q.getId().toString().equals(request.getQuestionId()));
        if (!isAssigned) {
            throw new RuntimeException("Question is not assigned to this interview");
        }

        Answer answer = answerRepository.findBySessionAndQuestionId(session, request.getQuestionId())
                .orElseGet(() -> {
                    Answer a = new Answer();
                    a.setSession(session);
                    a.setQuestionId(request.getQuestionId());
                    return a;
                });
        answer.setAnswerText(request.getAnswer());

        AiEvaluationResponse evaluation = evaluationService.evaluate(request.getAnswer(), request.getQuestionId());
        answer.setScore(evaluation.getScore());
        answer.setFeedback(evaluation.getFeedback());

        Answer saved = answerRepository.save(answer);

        if ("SCHEDULED".equals(session.getStatus())) {
            session.setStatus("IN_PROGRESS");
            sessionRepository.save(session);
        }

        AnswerDTO dto = mapToDTO(saved);
        dto.setScore(null);
        dto.setFeedback(null);
        return dto;
    }

    public InterviewSessionDTO submitInterview(Long sessionId, Long requesterId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getCandidate().getId().equals(requesterId)) {
            throw new RuntimeException("Access denied");
        }

        List<Answer> answers = answerRepository.findBySession(session);
        List<Long> answeredQuestionIds = answers.stream()
                .map(a -> Long.parseLong(a.getQuestionId()))
                .collect(Collectors.toList());

        boolean allAnswered = session.getAssignedQuestions().stream()
                .allMatch(q -> answeredQuestionIds.contains(q.getId()));
        if (!allAnswered) {
            throw new RuntimeException("All assigned questions must be answered before submitting");
        }

        session.setStatus("COMPLETED");
        session.setCompletedAt(LocalDateTime.now());
        sessionRepository.save(session);

        double averageScore = answers.stream()
                .mapToDouble(a -> a.getScore() != null ? a.getScore() : 0.0)
                .average()
                .orElse(0.0);

        Evaluation evaluation = evaluationRepository.findBySession(session).orElseGet(Evaluation::new);
        evaluation.setSession(session);
        evaluation.setTotalQuestionsAttempted(answers.size());
        evaluation.setAverageScore(averageScore);
        evaluation.setOverallScore(averageScore);
        evaluation.setSummary(answers.stream()
                .map(Answer::getFeedback)
                .filter(f -> f != null && !f.isBlank())
                .collect(Collectors.joining("\n\n")));
        evaluationRepository.save(evaluation);

        return mapToDTO(session, false);
    }

    public void attachRecording(Long sessionId, Long requesterId, MultipartFile file) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getCandidate().getId().equals(requesterId)) {
            throw new RuntimeException("Access denied");
        }

        recordingStorageService.save(sessionId, file);
        session.setRecordingPath("session-" + sessionId + ".webm");
        sessionRepository.save(session);
    }

    public UrlResource getRecording(Long sessionId, String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            throw new RuntimeException("Access denied: admin role required");
        }

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (session.getRecordingPath() == null) {
            throw new RuntimeException("No recording available for this session");
        }

        return recordingStorageService.load(sessionId)
                .orElseThrow(() -> new RuntimeException("Recording file missing on disk"));
    }

    private InterviewSessionDTO mapToDTO(InterviewSession session, boolean includeScores) {
        InterviewSessionDTO dto = new InterviewSessionDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setStatus(session.getStatus());
        dto.setCandidateId(session.getCandidate().getId());
        dto.setCandidateName(session.getCandidate().getName());
        dto.setCandidateEmail(session.getCandidate().getEmail());
        dto.setDueAt(session.getDueAt());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setCompletedAt(session.getCompletedAt());
        dto.setRecordingAvailable(session.getRecordingPath() != null);

        dto.setAssignedQuestions(session.getAssignedQuestions().stream()
                .map(q -> new AssignedQuestionDTO(q.getId(), q.getQuestionText(), q.getTopic(), q.getDifficulty()))
                .collect(Collectors.toList()));

        List<AnswerDTO> answers = answerRepository.findBySession(session).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        if (!includeScores) {
            answers.forEach(a -> {
                a.setScore(null);
                a.setFeedback(null);
            });
        }
        dto.setAnswers(answers);

        if (includeScores) {
            evaluationRepository.findBySession(session).ifPresent(e -> dto.setEvaluation(mapToDTO(e)));
        }

        return dto;
    }

    private AnswerDTO mapToDTO(Answer answer) {
        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setQuestionId(answer.getQuestionId());
        dto.setAnswerText(answer.getAnswerText());
        dto.setScore(answer.getScore());
        dto.setFeedback(answer.getFeedback());
        dto.setSubmittedAt(answer.getSubmittedAt());
        return dto;
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
