package com.example.recrutement.delegate;

import com.example.recrutement.services.EmailService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;

@Component("emailDelegate")
public class EmailDelegate implements JavaDelegate {
    @Autowired
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) {
        String candidateEmail = (String) execution.getVariable("candidateEmail");
        String subject = (String) execution.getVariable("emailSubject");
        String body = (String) execution.getVariable("emailBody");
        try {
            emailService.sendHtmlEmail(candidateEmail, subject, body);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}