package com.crm.app.billing.mail;

import com.crm.app.dto.Customer;
import com.crm.app.dto.InvoiceRecord;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingMailService {

    private final JavaMailSender mailSender;

    private static final String TIMEZONE = "Europe/Berlin";
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    public void sendSuccessMail(InvoiceRecord invoiceRecord) {
        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(invoiceRecord.getCustomer().emailAddress());
            helper.setSubject(String.format("Rechnung %s", invoiceRecord.getInvoiceNo()));
            helper.setFrom("CRM-Upload <support@crmupload.de>");
            helper.setReplyTo("CRM-Upload Support <support@crmupload.de>");

            helper.setText(bodySuccess(invoiceRecord), false);
            helper.addAttachment(String.format("Rechnung %s", invoiceRecord.getInvoiceNo()),
                    new ByteArrayResource(invoiceRecord.getInvoiceImage())
            );

            mailSender.send(message);
            log.info(String.format("Invoice mail sent to %s", invoiceRecord.getCustomer().emailAddress()));
        } catch (MessagingException e) {
            log.error(String.format("Invoice mail to send duplicate-check success to %s", invoiceRecord.getCustomer().emailAddress()), e);
        }
    }

    private String bodySuccess(InvoiceRecord invoiceRecord) {

        String fullName = Customer.getFullname(invoiceRecord.getCustomer());

        return """
                    Hallo %s,
                
                    anbei erhalten Sie Ihre Rechnung %s als PDF-Dokument.
                
                    Die beigefügte Rechnung ist ein elektronisches Rechnungsdokument im Standard EN 16931
                    (ZUGFeRD / Factur-X) und kann unverändert an Ihre Buchhaltung oder Ihren Steuerberater
                    weitergegeben werden.
                
                    Rechnungsdatum: %s
                    Fälligkeitsdatum: %s
                
                    Bitte überweisen Sie den Rechnungsbetrag bis zum angegebenen Fälligkeitsdatum.
                
                    Sollten Sie Fragen zu dieser Rechnung haben, antworten Sie bitte einfach auf diese E-Mail
                    oder wenden Sie sich an support@crmupload.de unter Angabe der Rechnungsnummer.
                
                    Vielen Dank für Ihr Vertrauen.
                
                    Freundliche Grüße
                    Ihr CRM-Upload-Team
                
                    CRM-Upload
                    support@crmupload.de
                    www.crmupload.de
                """.formatted(
                fullName,
                invoiceRecord.getInvoiceNo(),
                formatDate(invoiceRecord.getInvoiceDate()),
                formatDate(invoiceRecord.getInvoiceDueDate())
        );
    }

    private String formatDate(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.of(TIMEZONE))
                .format(Instant.ofEpochMilli(timestamp.getTime()));
    }
}
