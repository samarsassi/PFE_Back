package com.example.recrutement.delegate;

import com.example.recrutement.services.EmailService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;

@Component("sendInterviewInviteDelegate")
public class SendInterviewInviteDelegate implements JavaDelegate {
    @Autowired
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) {
        String candidateEmail = (String) execution.getVariable("candidateEmail");
        String interviewDate = (String) execution.getVariable("interviewDate");
        String emailBody = String.format("Your interview is scheduled for %s. Join here: http://localhost:4200/interview", interviewDate);
        try {
            emailService.sendHtmlEmail(candidateEmail, "Interview Scheduled", emailBody);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send interview invite: " + e.getMessage());
        }
    }
}