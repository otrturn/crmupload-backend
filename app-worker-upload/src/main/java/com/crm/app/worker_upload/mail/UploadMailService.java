package com.crm.app.worker_upload.mail;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
import com.crm.app.dto.AppConstants;
import com.crm.app.dto.CrmUploadContent;
import com.crm.app.port.customer.Customer;
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
    private final AppDataSourceProperties appDataSourceProperties;

    public void sendSuccessMailForEspo(Customer customer, CrmUploadContent upload, EspoEntityPool espoEntityPool) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten wurden in das CRM %s übertragen", upload.getSourceSystem(), upload.getCrmSystem()));
            helper.setText(bodySuccessForEspo(customer, upload.getSourceSystem(), upload.getCrmSystem(), espoEntityPool), false);

            helper.setFrom("noreply@crmupload.de");

            mailSender.send(message);
            log.info("Upload-Success mail sent to {}", customer.emailAddress());
        } catch (MessagingException e) {
            log.error("Upload-Error mail to send activation mail to {}", customer.emailAddress(), e);
        }
    }

    private String bodySuccessForEspo(Customer customer, String sourceSystem, String crmSystem, EspoEntityPool espoEntityPool) {

        StringBuilder sb = new StringBuilder();

        sb.append("Hallo ")
                .append(Customer.getFullname(customer))
                .append("\nIhre ").append(sourceSystem)
                .append("Daten wurden in das CRM ").append(crmSystem)
                .append("übertragen.\n\n")
                .append("Es wurden ").append(espoEntityPool.getAccounts().size())
                .append(" Firmen und ").append(espoEntityPool.getContacts().size())
                .append("Kontakte angelegt.\n\n");

        sb.append(AppConstants.RECOMMENDATION);

        sb.append("Viele Grüße\n").append("Ihr CRM -Upload - Team\n\n");

        return sb.toString();
    }

    public void sendErrorMailForEspo(Customer customer, CrmUploadContent upload, List<ErrMsg> errors, Path excelTargetfile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten müssen noch korrigiert werden", upload.getSourceSystem()));
            helper.setText(bodyFailedForEspo(customer, upload.getSourceSystem(), upload.getCrmSystem(), errors), false);

            helper.setFrom("noreply@crmupload.de");

            FileSystemResource file = new FileSystemResource(excelTargetfile.toFile());
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
            log.info("Activation mail sent to {}", customer.emailAddress());
        } catch (MessagingException e) {
            log.error("Failed to send activation mail to {}", customer.emailAddress(), e);
        }
    }

    private String bodyFailedForEspo(Customer customer, String sourceSystem, String crmSystem, List<ErrMsg> errors) {
        StringBuilder sb = new StringBuilder();

        sb.append("Hallo ")
                .append(Customer.getFullname(customer))
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

        sb.append(AppConstants.RECOMMENDATION);

        sb.append("\nViele Grüße\n")
                .append("Ihr CRM-Upload-Team\n");

        return sb.toString();
    }

}

