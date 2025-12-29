package com.crm.app.worker_upload.mail;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.Customer;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoEntityPool;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadMailService {

    private static final String TIMEZONE = "Europe/Berlin";
    private final JavaMailSender mailSender;

    private static final String LITERAL_FROM = "CRM-Upload <support@crmupload.de>";
    private static final String LITERAL_REPLY_TO = "CRM-Upload Support <support@crmupload.de>";
    private static final String LITERAL_UTF_8 = "UTF-8";
    private static final String LITERAL_DATE_FORMAT = "dd.MM.yyyy";


    public void sendSuccessMailForEspo(Customer customer, CrmUploadContent upload, EspoEntityPool espoEntityPool, EspoEntityPool espoEntityPoolForIgnore) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, LITERAL_UTF_8);

            helper.setTo(customer.emailAddress());
            helper.setSubject(
                    String.format(
                            "Upload erfolgreich: %s → %s (Upload-ID %d)",
                            upload.getSourceSystem(),
                            upload.getCrmSystem(),
                            upload.getUploadId()
                    )
            );
            helper.setText(bodySuccessForEspo(customer, upload.getUploadId(), upload.getSourceSystem(), upload.getCrmSystem(), espoEntityPool, espoEntityPoolForIgnore), false);
            helper.setFrom(LITERAL_FROM);
            helper.setReplyTo(LITERAL_REPLY_TO);

            mailSender.send(message);
            log.info(String.format("Upload-Success mail sent to %s", customer.emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Failed mail to send Upload-Success mail to %s", customer.emailAddress()), e);
        }
    }

    private String bodySuccessForEspo(Customer customer, long uploadId, String sourceSystem, String crmSystem, EspoEntityPool espoEntityPool, EspoEntityPool espoEntityPoolForIgnore) {
        return """
                Hallo %s,
                
                Ihre %s-Daten wurden erfolgreich in das CRM %s übertragen.
                
                Ergebnis der Übertragung:
                - Firmen angelegt: %d
                - Firmen ignoriert: %d (bereits vorhanden)
                - Kontakte angelegt: %d
                - Kontakte ignoriert: %d (bereits vorhanden)
                
                Upload-ID: %d
                Datum der Übertragung: %s
                
                Die Übertragung gilt als technisch erfolgreich, sobald Datensätze
                vom Ziel-CRM angenommen wurden. Eine inhaltliche oder geschäftliche
                Bewertung der Daten ist damit nicht verbunden.
                
                Die für diesen Upload vorübergehend gespeicherten Daten werden nach
                Abschluss der Übertragung gemäß unseren Allgemeinen Geschäftsbedingungen
                gelöscht, soweit keine gesetzlichen Aufbewahrungspflichten entgegenstehen.
                
                Den Status Ihrer Uploads können Sie jederzeit in Ihrem Nutzerkonto
                einsehen.
                
                Sollten Sie diese Übertragung nicht selbst ausgelöst haben oder Fragen
                zur Verarbeitung haben, kontaktieren Sie uns bitte unter:
                support@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                www.crmupload.de
                """.formatted(Customer.getFullname(customer), sourceSystem, crmSystem,
                espoEntityPool.getAccounts().size(), espoEntityPoolForIgnore.getAccounts().size(),
                espoEntityPool.getContacts().size(), espoEntityPoolForIgnore.getContacts().size(),
                uploadId,
                DateTimeFormatter
                        .ofPattern(LITERAL_DATE_FORMAT)
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.now()));
    }

    public void sendErrorMailForEspo(Customer customer, CrmUploadContent upload, List<ErrMsg> errors, byte[] errorFile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, LITERAL_UTF_8);

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format("Ihre %s Daten müssen noch korrigiert werden (Upload-ID %d)", upload.getSourceSystem(), upload.getUploadId()));
            helper.setText(bodyFailedForEspo(customer, upload.getUploadId(), Instant.now(), upload.getSourceSystem(), upload.getCrmSystem(), errors), false);
            helper.setFrom(LITERAL_FROM);
            helper.setReplyTo(LITERAL_REPLY_TO);

            helper.addAttachment(
                    String.format("Upload_Ergebnis_%s_%s.xlsx",
                            upload.getSourceSystem(),
                            DateTimeFormatter.ofPattern("yyyyMMdd")
                                    .withZone(ZoneId.of(TIMEZONE))
                                    .format(Instant.now())
                    ),
                    new ByteArrayResource(errorFile)
            );
            mailSender.send(message);
            log.info(String.format("Upload-Error mail sent to %s", customer.emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Failed to send Upload-Error mail to %s", customer.emailAddress()), e);
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

        int max = 50;
        int count = 0;

        for (ErrMsg error : errors) {
            if (count++ >= max) break;
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

        if (errors.size() > max) {
            sb.append(String.format("… sowie %d weitere Hinweise (siehe Markierungen in der Datei).",
                    errors.size() - max));
            sb.append("\n");
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
                        .ofPattern(LITERAL_DATE_FORMAT)
                        .withZone(ZoneId.systemDefault())
                        .format(uploadDate)));

        return sb.toString();
    }

    public void sendErrorMailForEspoSanityCheck(Customer customer, CrmUploadContent upload) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, LITERAL_UTF_8);

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format("CRM-Upload von %s nicht möglich – technische Voraussetzungen nicht erfüllt (Upload-ID %d)",
                    upload.getSourceSystem(),
                    upload.getUploadId()));
            helper.setText(bodyForSanityCheck(customer, upload.getUploadId(), Instant.now(), upload.getSourceSystem(), upload.getCrmSystem()), false);
            helper.setFrom(LITERAL_FROM);
            helper.setReplyTo(LITERAL_REPLY_TO);
            mailSender.send(message);
            log.info(String.format("Sanity-Check mail sent to %s", customer.emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Failed to send Sanity-Check mail to %s", customer.emailAddress()), e);
        }
    }

    private String bodyForSanityCheck(Customer customer, long uploadId, Instant uploadDate, String sourceSystem, String crmSystem) {

        return """
                Hallo %s,
                Ihr %s-Export konnte noch nicht in das CRM %s übertragen werden.
                Unsere automatische Systemprüfung hat ergeben, dass eine oder mehrere technische Voraussetzungen in Ihrem CRM nicht erfüllt sind.
                
                Der Upload wurde daher nicht durchgeführt, um fehlerhafte oder unvollständige Daten zu vermeiden.
                
                """.formatted(Customer.getFullname(customer), sourceSystem, crmSystem) +
                """
                        Upload-ID: %d
                        Datum: %s
                        
                        ➡ Was jetzt zu tun ist
                        
                        Bitte wenden Sie sich an den Betreiber oder Administrator Ihres %s-Systems.
                        Die erforderlichen technischen Voraussetzungen finden Sie hier:
                        
                        https://www.crmupload.de/hilfe
                        
                        Sollten Sie diesen Upload nicht selbst ausgelöst haben, ignorieren Sie diese E-Mail bitte
                        und informieren Sie uns unter: support@crmupload.de
                        
                        Bei Fragen helfen wir Ihnen selbstverständlich gerne weiter.
                        
                        Viele Grüße
                        Ihr CRM-Upload-Team
                        support@crmupload.de
                        www.crmupload.de
                        """.formatted(uploadId,
                        DateTimeFormatter
                                .ofPattern(LITERAL_DATE_FORMAT)
                                .withZone(ZoneId.systemDefault())
                                .format(uploadDate),
                        crmSystem);
    }

}
