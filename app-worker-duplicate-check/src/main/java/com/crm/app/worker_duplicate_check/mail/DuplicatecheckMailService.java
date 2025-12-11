package com.crm.app.worker_duplicate_check.mail;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.Customer;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoEntityPool;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicatecheckMailService {

    private final JavaMailSender mailSender;

    public void sendErrorMail(Customer customer, DuplicateCheckContent duplicateCheckContent, List<ErrMsg> errors, byte[] errorFile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten müssen noch korrigiert werden", duplicateCheckContent.getSourceSystem()));
            helper.setText(bodyFailed(customer, duplicateCheckContent.getSourceSystem(),  errors), false);

            helper.setFrom("noreply@crmupload.de");

            ByteArrayResource resource = new ByteArrayResource(errorFile);
            helper.addAttachment("Korrektur.xlsx", resource);

            mailSender.send(message);
            log.info("Activation mail sent to {}", customer.emailAddress());
        } catch (MessagingException e) {
            log.error("Failed to send activation mail to {}", customer.emailAddress(), e);
        }
    }

    private String bodyFailed(Customer customer, String sourceSystem, List<ErrMsg> errors) {
        StringBuilder sb = new StringBuilder();

        sb.append("Hallo ")
                .append(Customer.getFullname(customer))
                .append(",\n\n")
                .append("Ihre ")
                .append(sourceSystem)
                .append(" Daten konnten noch nicht für di Dublettenprüfugn vorbereitet werden\n")
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

