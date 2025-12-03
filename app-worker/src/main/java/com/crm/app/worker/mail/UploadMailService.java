package com.crm.app.worker.mail;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.worker.util.WorkerUtils;
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
public class UploadMailService {

    private final JavaMailSender mailSender;

    public void sendSuccessMail(Consumer consumer, ConsumerUploadContent upload) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(consumer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten wurden in das CRM %s übertragen", upload.sourceSystem(), upload.crmSystem()));
            helper.setText(bodySuccess(consumer, upload.sourceSystem(), upload.crmSystem()), false);

            helper.setFrom("noreply@crmupload.de");

            mailSender.send(message);
            log.info("Activation mail sent to {}", consumer.emailAddress());
        } catch (MessagingException e) {
            log.error("Failed to send activation mail to {}", consumer.emailAddress(), e);
        }
    }

    private String bodySuccess(Consumer consumer, String sourceSystem, String crmSystem) {
        return """
                Hallo %s,
                
                Ihr %s Daten wurden in das CRM %s übertragen.
                
                Viele Grüße
                Ihr CRM-Upload-Team
                """.formatted(WorkerUtils.getFullname(consumer), sourceSystem, crmSystem);
    }
}

