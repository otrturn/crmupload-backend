package com.crm.app.worker_duplicate_check.mail;

import com.crm.app.dto.Customer;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.worker_common.dto.StatisticsDuplicateCheck;
import com.crmmacher.error.ErrMsg;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DuplicatecheckMailService {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    private static final String TIMEZONE = "Europe/Berlin";
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    public void sendSuccessMail(Customer customer, DuplicateCheckContent duplicateCheckContent, byte[] resultFile) {
        StatisticsDuplicateCheck statistics = null;
        try {
            log.info("Stat=[" + duplicateCheckContent.getStatistics() + "]");
            statistics = objectMapper.readValue(duplicateCheckContent.getStatistics(), StatisticsDuplicateCheck.class);
        } catch (Exception e) {
            log.error(String.format("ObjectMapper error mail to send duplicate-check success mail to %s", customer.emailAddress()), e);
        }

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format(
                    "Dublettenprüfung abgeschlossen – %s (Prüf-ID %s, Stand %s)",
                    duplicateCheckContent.getSourceSystem(),
                    duplicateCheckContent.getDuplicateCheckId(),
                    DateTimeFormatter.ofPattern(DATE_FORMAT)
                            .withZone(ZoneId.of(TIMEZONE))
                            .format(Instant.now())
            ));
            helper.setFrom("CRM-Upload <support@crmupload.de>");
            helper.setReplyTo("CRM-Upload Support <support@crmupload.de>");

            boolean findings = statistics != null && hasFindings(statistics);

            if (findings) {
                helper.setText(bodySuccessWithFindings(customer, duplicateCheckContent, statistics), false);
                helper.addAttachment(
                        String.format("Dublettenpruefung_Ergebnis_%s_%s.xlsx",
                                duplicateCheckContent.getSourceSystem(),
                                DateTimeFormatter.ofPattern("yyyyMMdd")
                                        .withZone(ZoneId.of(TIMEZONE))
                                        .format(Instant.now())
                        ),
                        new ByteArrayResource(resultFile)
                );
            } else {
                helper.setText(bodySuccessNoFindings(customer, duplicateCheckContent), false);
            }

            mailSender.send(message);
            log.info(String.format("Duplicate check success mail sent to %s", customer.emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Duplicate check error mail to send duplicate-check success to %s", customer.emailAddress()), e);
        }
    }

    private String bodySuccessWithFindings(Customer customer,
                                           DuplicateCheckContent content,
                                           StatisticsDuplicateCheck statistics) {

        String date = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.of(TIMEZONE))
                .format(Instant.now());

        String statisticsText = statisticsBlock(statistics);

        return """
                Hallo %s,
                
                Ihre %s-Daten wurden erfolgreich auf mögliche Dubletten geprüft.
                Das Ergebnis finden Sie im Anhang.
                
                Prüf-ID: %s
                Datum: %s
                %s
                Hinweis zum Datenschutz:
                Der Anhang kann personenbezogene Daten enthalten. Bitte behandeln Sie die Datei vertraulich
                und löschen Sie sie nach Abschluss der Prüfung.
                
                Falls Sie diese Dublettenprüfung nicht selbst ausgelöst haben, ignorieren Sie diese E-Mail bitte
                und informieren Sie uns unter: support@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                support@crmupload.de
                www.crmupload.de
                """.formatted(
                Customer.getFullname(customer),
                content.getSourceSystem(),
                content.getDuplicateCheckId(),
                date,
                statisticsText
        );
    }

    private String bodySuccessNoFindings(Customer customer,
                                         DuplicateCheckContent content) {

        String date = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.of(TIMEZONE))
                .format(Instant.now());

        return """
                Hallo %s,
                
                Ihre %s-Daten wurden erfolgreich auf mögliche Dubletten geprüft.
                
                Ergebnis:
                Es wurden keine Dubletten oder auffälligen Mehrfacheinträge festgestellt.
                Ihre Datenbasis ist aus technischer Sicht konsistent.
                
                Prüf-ID: %s
                Datum: %s
                
                Es wurde keine Ergebnisdatei erzeugt, da keine Dubletten festgestellt wurden.
                
                Hinweis zum Datenschutz:
                Die Prüfung erfolgte ausschließlich im Rahmen Ihres Auftrags.
                Es wurden keine Daten dauerhaft gespeichert.
                
                Falls Sie diese Dublettenprüfung nicht selbst ausgelöst haben,
                ignorieren Sie diese E-Mail bitte und informieren Sie uns unter:
                support@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                support@crmupload.de
                www.crmupload.de
                """.formatted(
                Customer.getFullname(customer),
                content.getSourceSystem(),
                content.getDuplicateCheckId(),
                date
        );
    }

    private String statisticsBlock(StatisticsDuplicateCheck s) {
        StringBuilder sb = new StringBuilder();

        if (s.getNEntries() > 0) {
            sb.append("• Verarbeitete Einträge: ")
                    .append(s.getNEntries())
                    .append("\n");
        }
        if (s.getNDuplicateAccountNames() > 0) {
            sb.append("• Wahrscheinliche Dubletten bei Firmennamen: ")
                    .append(s.getNDuplicateAccountNames())
                    .append("\n");
        }
        if (s.getNAddressMatchesProbable() > 0) {
            sb.append("• Wahrscheinlich übereinstimmende Adressen: ")
                    .append(s.getNAddressMatchesProbable())
                    .append("\n");
        }
        if (s.getNAddressMatchesPossible() > 0) {
            sb.append("• Möglicherweise übereinstimmende Adressen: ")
                    .append(s.getNAddressMatchesPossible())
                    .append("\n");
        }
        if (s.getNEmailMatches() > 0) {
            sb.append("• Mehrfach verwendete E-Mail-Adressen: ")
                    .append(s.getNEmailMatches())
                    .append("\n");
        }

        if (sb.isEmpty()) {
            return "";
        }

        return """
                
                Zusammenfassung der Prüfung:
                %s
                """.formatted(sb.toString());
    }

    public void sendErrorMail(Customer customer, DuplicateCheckContent duplicateCheckContent, List<ErrMsg> errors, byte[] errorFile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customer.emailAddress());
            helper.setSubject(String.format(
                    "Dublettenprüfung nicht möglich – %s (Prüf-ID %s, Stand %s)",
                    duplicateCheckContent.getSourceSystem(),
                    duplicateCheckContent.getDuplicateCheckId(),
                    DateTimeFormatter.ofPattern(DATE_FORMAT)
                            .withZone(ZoneId.of(TIMEZONE))
                            .format(Instant.now())
            ));
            helper.setText(bodyFailed(customer, duplicateCheckContent.getSourceSystem(), errors), false);
            helper.setFrom("CRM-Upload <support@crmupload.de>");
            helper.setReplyTo("CRM-Upload Support <support@crmupload.de>");

            helper.addAttachment(
                    String.format("Dublettenpruefung_Korrektur_%s_%s.xlsx",
                            duplicateCheckContent.getSourceSystem(),
                            DateTimeFormatter.ofPattern("yyyyMMdd")
                                    .withZone(ZoneId.of(TIMEZONE))
                                    .format(Instant.now())
                    ),
                    new ByteArrayResource(errorFile)
            );
            mailSender.send(message);
            log.info(String.format("Error mail sent to %s", customer.emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Failed to send duplicate check success mail to %s", customer.emailAddress()), e);
        }
    }

    private String bodyFailed(Customer customer, String sourceSystem, List<ErrMsg> errors) {
        var date = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.of(TIMEZONE))
                .format(Instant.now());

        StringBuilder sb = new StringBuilder();
        sb.append("""
                Hallo %s,
                
                Ihre %s-Dublettenprüfung konnte noch nicht durchgeführt werden,
                weil in der Datei Pflichtangaben fehlen oder einzelne Werte ungültig sind.
                
                Im Anhang finden Sie Ihre Exceldatei mit markierten Feldern sowie die notwendigen Korrekturen:
                """.formatted(Customer.getFullname(customer), sourceSystem));

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
                
                Datum: %s
                
                Bitte korrigieren Sie die markierten Stellen und laden Sie die Datei anschließend erneut hoch.
                
                Hinweis zum Datenschutz:
                Der Anhang kann personenbezogene Daten enthalten. Bitte behandeln Sie die Datei vertraulich
                und löschen Sie sie nach Abschluss der Korrektur.
                
                Falls Sie diese Dublettenprüfung nicht selbst ausgelöst haben, ignorieren Sie diese E-Mail bitte
                und informieren Sie uns unter: support@crmupload.de
                
                Viele Grüße
                Ihr CRM-Upload-Team
                support@crmupload.de
                www.crmupload.de
                """.formatted(date));

        return sb.toString();
    }

    private boolean hasFindings(StatisticsDuplicateCheck s) {
        return s.getNDuplicateAccountNames() > 0
                || s.getNAddressMatchesProbable() > 0
                || s.getNAddressMatchesPossible() > 0
                || s.getNEmailMatches() > 0;
    }
}
