package com.crm.app.web.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationMailService {

    private final JavaMailSender mailSender;

    public void sendActivationMail(String recipientEmail, String name, String activationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject("Bitte Account freischalten");
            helper.setText(buildBody(name, activationLink), false);

            helper.setFrom("noreply@crmupload.de");

            mailSender.send(message);
            log.info(String.format("Activation mail sent to %s", recipientEmail));
        } catch (MessagingException e) {
            log.error(String.format("Activation error mail to send activation mail to %s", recipientEmail), e);
            // Je nach Philosophie:
            // - Registrierung trotzdem als „pending“ stehen lassen
            // - oder Fehler weiterwerfen
        }
    }

    private String buildBody(String name, String activationLink) {
        return """
                Hallo %s,
                
                danke für Ihre Registrierung bei CRM-Upload.
                
                Bitte klicken Sie auf den folgenden Link, um Ihr Konto freizuschalten:
                
                %s
                
                Viele Grüße
                Ihr CRM-Upload-Team
                """.formatted(name, activationLink);
    }
}
