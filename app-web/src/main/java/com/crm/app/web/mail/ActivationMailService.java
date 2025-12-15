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
            helper.setSubject("„Betreff: Bitte bestätigen Sie Ihre Registrierung bei CRM-Upload“");
            helper.setText(buildActivationBody(name, activationLink), false);

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

    private String buildActivationBody(String name, String activationLink) {
        return """
                Hallo %s,
                
                vielen Dank für Ihre Registrierung bei CRM-Upload.
                
                Bitte klicken Sie auf den folgenden Link, um Ihr Benutzerkonto
                freizuschalten:
                
                %s
                
                Sollten Sie sich nicht selbst registriert haben, können Sie diese
                E-Mail ignorieren. Ihr Konto wird dann nicht aktiviert.
                
                Bei Fragen erreichen Sie uns unter:
                info@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                www.crmupload.de
                """.formatted(name, activationLink);
    }

    public void sendConfirmationMail(String recipientEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject("„Betreff: Ihr CRM-Upload-Konto wurde aktiviert“");
            helper.setText(buildConfirmationBody(name), false);

            helper.setFrom("noreply@crmupload.de");

            mailSender.send(message);
            log.info(String.format("Confirmation mail sent to %s", recipientEmail));
        } catch (MessagingException e) {
            log.error(String.format("Confirmation error mail to send activation mail to %s", recipientEmail), e);
        }
    }

    private String buildConfirmationBody(String name) {
        return """
                Hallo %s,
                
                Ihr Benutzerkonto bei CRM-Upload wurde erfolgreich aktiviert.
                
                Sie können sich ab sofort mit Ihrer E-Mail-Adresse anmelden
                und die angebotenen Leistungen nutzen.
                
                Sollten Sie diese Aktivierung nicht selbst vorgenommen haben,
                kontaktieren Sie uns bitte umgehend unter:
                info@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                www.crmupload.de
                """.formatted(name);
    }

}
