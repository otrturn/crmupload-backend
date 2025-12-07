package com.crm.app.worker.mail;

import com.crm.app.dto.ConsumerUploadContent;
import com.crm.app.port.consumer.Consumer;
import com.crm.app.worker.util.WorkerUtils;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoEntityPool;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadMailService {

    private final JavaMailSender mailSender;

    public void sendSuccessMailForEspo(Consumer consumer, ConsumerUploadContent upload, EspoEntityPool espoEntityPool) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(consumer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten wurden in das CRM %s übertragen", upload.sourceSystem(), upload.crmSystem()));
            helper.setText(bodySuccessForEspo(consumer, upload.sourceSystem(), upload.crmSystem(), espoEntityPool), false);

            helper.setFrom("noreply@crmupload.de");

            mailSender.send(message);
            log.info("Activation mail sent to {}", consumer.emailAddress());
        } catch (MessagingException e) {
            log.error("Failed to send activation mail to {}", consumer.emailAddress(), e);
        }
    }

    private String bodySuccessForEspo(Consumer consumer, String sourceSystem, String crmSystem, EspoEntityPool espoEntityPool) {
        return """
                Hallo %s,
                
                Ihr %s Daten wurden in das CRM %s übertragen.
                
                Es wurden %d Firmen und %d Kontakte angelegt.
                
                Viele Grüße
                Ihr CRM-Upload-Team
                """.formatted(WorkerUtils.getFullname(consumer), sourceSystem, crmSystem,
                espoEntityPool.getAccounts().size(), espoEntityPool.getContacts().size());
    }

    public void sendErrorMailForEspo(Consumer consumer, ConsumerUploadContent upload, List<ErrMsg> errors, Path excelTargetfile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(consumer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten müssen noch korrigiert werden", upload.sourceSystem()));
            helper.setText(bodyFailedForEspo(consumer, upload.sourceSystem(), upload.crmSystem(), errors), false);

            helper.setFrom("noreply@crmupload.de");

            FileSystemResource file = new FileSystemResource(excelTargetfile.toFile());
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
            log.info("Activation mail sent to {}", consumer.emailAddress());
        } catch (MessagingException e) {
            log.error("Failed to send activation mail to {}", consumer.emailAddress(), e);
        }
    }

    private String bodyFailedForEspo(Consumer consumer, String sourceSystem, String crmSystem, List<ErrMsg> errors) {
        StringBuilder sb = new StringBuilder();

        sb.append("Hallo ")
                .append(WorkerUtils.getFullname(consumer))
                .append(",\n\n")
                .append("Ihre ")
                .append(sourceSystem)
                .append(" Daten konnten noch nicht in das CRM ")
                .append(crmSystem)
                .append(" übertragen werden.\n\n")
                .append("Folgende Korrekturen müssen noch vorgenommen werden:\n");

        for (ErrMsg error : errors) {
            sb.append("- Arbeitsblatt ")
                    .append(error.getSheetNum() + 1)
                    .append(" Zeile ")
                    .append(error.getRowNum() + 1)
                    .append(" Spalte ")
                    .append(error.getColNum() + 1)
                    .append(": ")
                    .append(error.getMessage())
                    .append("\n");
        }

        sb.append("\nViele Grüße\n")
                .append("Ihr CRM-Upload-Team\n");

        return sb.toString();
    }

}

