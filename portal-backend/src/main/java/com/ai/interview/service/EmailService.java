package com.ai.interview.service;

import com.ai.interview.entity.InterviewSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter DUE_AT_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

    private final JavaMailSender mailSender;

    @Value("${notifications.email.from:}")
    private String fromAddress;

    @Value("${portal.frontend-url}")
    private String portalUrl;

    public void sendInterviewScheduledEmail(InterviewSession session) {
        if (fromAddress == null || fromAddress.isBlank()) {
            log.warn("Skipping interview-scheduled email for session {}: MAIL_USERNAME/MAIL_FROM not configured", session.getId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(session.getCandidate().getEmail());
            message.setSubject("Your interview has been scheduled: " + session.getTitle());
            message.setText(buildBody(session));
            mailSender.send(message);
            log.info("Sent interview-scheduled email for session {} to {}", session.getId(), session.getCandidate().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send interview-scheduled email for session {}: {}", session.getId(), e.getMessage());
        }
    }

    private String buildBody(InterviewSession session) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(session.getCandidate().getName()).append(",\n\n");
        sb.append("An interview has been scheduled for you: ").append(session.getTitle()).append("\n\n");

        if (session.getDueAt() != null) {
            sb.append("Due by: ").append(session.getDueAt().format(DUE_AT_FORMAT)).append("\n\n");
        }

        sb.append("Log in to the interview portal to attend your interview:\n");
        sb.append(portalUrl).append("\n\n");
        sb.append("Username: ").append(session.getCandidate().getEmail()).append("\n");
        sb.append("Password: ").append(session.getCandidate().getPassword()).append("\n");
        return sb.toString();
    }
}
