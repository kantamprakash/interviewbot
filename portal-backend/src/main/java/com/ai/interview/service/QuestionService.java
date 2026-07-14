package com.ai.interview.service;

import com.ai.interview.dto.CreateQuestionRequest;
import com.ai.interview.dto.QuestionDTO;
import com.ai.interview.entity.Question;
import com.ai.interview.entity.User;
import com.ai.interview.repository.QuestionRepository;
import com.ai.interview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public QuestionDTO createQuestion(CreateQuestionRequest request, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can create questions");
        }

        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setTopic(request.getTopic());
        question.setDifficulty(request.getDifficulty());
        question.setExpectedKeyPoints(request.getExpectedKeyPoints());
        question.setCreatedByUserId(adminUserId);
        question.setIsActive(true);

        Question saved = questionRepository.save(question);
        return mapToDTO(saved);
    }

    public List<QuestionDTO> getAllActiveQuestions() {
        return questionRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getQuestionsByTopic(String topic) {
        return questionRepository.findByTopicAndIsActiveTrueOrderByDifficulty(topic)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getQuestionsByDifficulty(String difficulty) {
        return questionRepository.findByDifficultyAndIsActiveTrueOrderByCreatedAtDesc(difficulty)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getQuestionsByTopicAndDifficulty(String topic, String difficulty) {
        return questionRepository.findByTopicAndDifficultyAndIsActiveTrue(topic, difficulty)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public QuestionDTO getQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return mapToDTO(question);
    }

    public QuestionDTO updateQuestion(Long questionId, CreateQuestionRequest request, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can update questions");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setQuestionText(request.getQuestionText());
        question.setTopic(request.getTopic());
        question.setDifficulty(request.getDifficulty());
        question.setExpectedKeyPoints(request.getExpectedKeyPoints());

        Question updated = questionRepository.save(question);
        return mapToDTO(updated);
    }

    public void deleteQuestion(Long questionId, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can delete questions");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setIsActive(false);
        questionRepository.save(question);
    }

    private QuestionDTO mapToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setTopic(question.getTopic());
        dto.setDifficulty(question.getDifficulty());
        dto.setExpectedKeyPoints(question.getExpectedKeyPoints());
        dto.setIsActive(question.getIsActive());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        return dto;
    }
}
