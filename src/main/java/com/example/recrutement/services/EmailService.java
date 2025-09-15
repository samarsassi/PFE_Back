package com.example.recrutement.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);  // true = HTML enabled
        helper.setFrom("your_email@gmail.com");
        mailSender.send(message);
    }

    public String buildChallengeAssignmentEmail(String candidateName, String challengeTitle, String portalLink) {
        String template = """
        <html>
        <body style="font-family: Arial, sans-serif; background-color:#f4f4f7; margin:0; padding:20px;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:600px; margin:auto; background:#ffffff; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1);">
            <tr>
              <td style="padding:20px; text-align:center; background-color:#4a90e2; color:#fff; border-radius:8px 8px 0 0;">
                <h1>Défi Technique Assigné</h1>
              </td>
            </tr>
            <tr>
              <td style="padding:20px; color:#333333;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Vous avez reçu un nouveau défi technique : <em>%s</em>.</p>
                <p>Pour accéder au défi et commencer, veuillez vous connecter à votre espace candidat en cliquant sur le bouton ci-dessous.</p>
                <p style="text-align:center; margin:30px 0;">
                  <a href="%s" style="background-color:#4a90e2; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; font-weight:bold;">Accéder au défi</a>
                </p>
                <p><strong>Note :</strong> Vous avez 48 heures pour compléter ce défi avant son expiration.</p>
                <p>Bonne chance !</p>
                <p>Cordialement,<br>L'équipe Recrutement</p>
              </td>
            </tr>
            <tr>
              <td style="padding:15px; font-size:12px; color:#777777; text-align:center;">
                © 2025 Votre Entreprise. Tous droits réservés.
              </td>
            </tr>
          </table>
        </body>
        </html>
        """;
        return String.format(template, candidateName, challengeTitle, portalLink);
    }
}
