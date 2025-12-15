package com.crm.app.worker_upload.mail;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            helper.setSubject(
                    String.format(
                            "Upload erfolgreich: %s → %s (Upload-ID %s)",
                            upload.getSourceSystem(),
                            upload.getCrmSystem(),
                            upload.getUploadId()
                    )
            );
            helper.setText(bodySuccessForEspo(customer, upload.getUploadId(), Instant.now(), upload.getSourceSystem(), upload.getCrmSystem(), espoEntityPool), false);
            helper.setFrom("CRM-Upload <support@crmupload.de>");
            helper.setReplyTo("CRM-Upload Support <support@crmupload.de>");

            mailSender.send(message);
            log.info(String.format("Upload-Success mail sent to %s", customer.emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Upload-Error mail to send activation mail to %s", customer.emailAddress()), e);
        }
    }

    private String bodySuccessForEspo(Customer customer, long uploadId, Instant uploadDate, String sourceSystem, String crmSystem, EspoEntityPool espoEntityPool) {
        return """
                Hallo %s,
                
                Ihre %s-Daten wurden erfolgreich in das CRM %s übertragen.
                
                Ergebnis der Übertragung:
                - Firmen angelegt: %d
                - Kontakte angelegt: %d
                
                Upload-ID: %d
                Datum der Übertragung: %s
                
                Die Übertragung gilt als technisch erfolgreich, sobald Datensätze vom Ziel-CRM angenommen wurden.
                
                Sie können sich jederzeit in Ihrem Nutzerkonto anmelden, um den Status
                und weitere Details einzusehen.
                
                Sollten Sie diese Übertragung nicht selbst ausgelöst haben oder Fragen
                zur Verarbeitung haben, kontaktieren Sie uns bitte unter:
                support@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                www.crmupload.de
                """.formatted(Customer.getFullname(customer), sourceSystem, crmSystem, espoEntityPool.getAccounts().size(), espoEntityPool.getContacts().size(),
                uploadId, DateTimeFormatter
                        .ofPattern("dd.MM.yyyy")
                        .withZone(ZoneId.systemDefault())
                        .format(uploadDate));
    }

    public void sendErrorMailForEspo(Customer customer, CrmUploadContent upload, List<ErrMsg> errors, Path excelTargetfile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten müssen noch korrigiert werden", upload.getSourceSystem()));
            helper.setText(bodyFailedForEspo(customer, upload.getUploadId(), Instant.now(), upload.getSourceSystem(), upload.getCrmSystem(), errors), false);
            helper.setFrom("CRM-Upload <support@crmupload.de>");
            helper.setReplyTo("CRM-Upload Support <support@crmupload.de>");

            FileSystemResource file = new FileSystemResource(excelTargetfile.toFile());
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
            log.info(String.format("CorrectionMail mail sent to %s", customer.emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Failed to send CorrectionMail mail to %s", customer.emailAddress()), e);
        }
    }

    private String bodyFailedForEspo(Customer customer, long uploadId, Instant uploadDate, String sourceSystem, String crmSystem, List<ErrMsg> errors) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                Hallo %s,
                Ihr %s-Export konnte noch nicht in das CRM %s übertragen werden,
                weil in der Datei Pflichtangaben fehlen oder einzelne Werte ungültig sind.
                Im Anhang finden Sie Ihre Exceldatei mit markierten Feldern sowie die notwendigen Korrekturen:
                """.formatted(Customer.getFullname(customer), sourceSystem, crmSystem));

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

        sb.append("""
                Upload-ID: %d
                Datum: %s
                
                Bitte korrigieren Sie die markierten Stellen und laden Sie die Datei anschließend erneut hoch.
                
                Hinweis zum Datenschutz:
                Der Anhang kann personenbezogene Daten enthalten. Bitte behandeln Sie die Datei vertraulich
                und löschen Sie sie nach Abschluss der Korrektur.
                
                Falls Sie diesen Upload nicht selbst ausgelöst haben, ignorieren Sie diese E-Mail bitte
                und informieren Sie uns unter: support@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                support@crmupload.de
                www.crmupload.de
                """.formatted(uploadId,
                DateTimeFormatter
                        .ofPattern("dd.MM.yyyy")
                        .withZone(ZoneId.systemDefault())
                        .format(uploadDate)));

        return sb.toString();
    }
}
