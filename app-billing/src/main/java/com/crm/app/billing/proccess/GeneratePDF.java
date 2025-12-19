package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import com.crm.app.billing.error.BillingGeneratePDFException;
import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerProduct;
import com.crm.app.dto.InvoiceRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.crm.app.dto.Customer.getFullname;

@Slf4j
@Component
public class GeneratePDF {

    private final AppBillingConfig appBillingConfig;

    private static final String NAME_OF_COMPANY = "Ralf Scholler";
    private static final String STREET_OF_COMPANY = "Am Dorfplatz 6";
    private static final String ZIP_CITY_OF_COMPANY = "57610 Ingelbach";
    private static final String TAX_NUMBER_OF_COMPANY = "Steuernummer 003/867/30663";
    private static final String VAT_ID_NUMBER_OF_COMPANY = "USt-Id-Nummer DE 244 3344 16";
    private static final String FORMAT_TWO_DECIMALS = "€%.2f";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String LITERAL_RECHNUNG = "Rechnung ";

    public GeneratePDF(AppBillingConfig appBillingConfig) {
        this.appBillingConfig = appBillingConfig;
    }

    public byte[] generatePDFForCustomer(InvoiceRecord invoiceRecord) throws BillingGeneratePDFException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            setDocumentHeaderInformation(document, invoiceRecord);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            setPageFooter(contentStream);
            setCustomerAddress(contentStream, invoiceRecord);
            setInvoiceHeader(contentStream, invoiceRecord);
            setInvoiceIntroduction(contentStream, invoiceRecord);
            setInvoicePositions(contentStream, invoiceRecord);
            setBankInformation(contentStream, invoiceRecord);
            setInvoiceFinish(contentStream);

            contentStream.close();
            document.save(appBillingConfig.getWorkdir() + "/" + String.format("Rechnung_%06d.pdf", invoiceRecord.getInvoiceNo()));
            return toPdfBytes(document);
        } catch (IOException e) {
            log.error("generatePDFForCustomer", e);
            throw new BillingGeneratePDFException("generatePDFForCustomer", e);
        }
    }

    private void setDocumentHeaderInformation(PDDocument document, InvoiceRecord invoiceRecord) {
        PDDocumentInformation pdd = document.getDocumentInformation();
        pdd.setAuthor(NAME_OF_COMPANY);
        pdd.setTitle(LITERAL_RECHNUNG + invoiceRecord.getInvoiceNoAsText() + " für " + getFullname(invoiceRecord.getCustomer()));
        pdd.setCreator(NAME_OF_COMPANY);
        pdd.setSubject(LITERAL_RECHNUNG + invoiceRecord.getInvoiceNoAsText() + " für " + getFullname(invoiceRecord.getCustomer()));
        Calendar date = new GregorianCalendar();
        date.setTime(new Date());
        pdd.setCreationDate(date);
    }

    private void setPageFooter(PDPageContentStream contentStream) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
        contentStream.newLineAtOffset(30, 30);
        contentStream.showText(NAME_OF_COMPANY);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
        contentStream.newLineAtOffset(180, 30);
        contentStream.showText(STREET_OF_COMPANY);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
        contentStream.newLineAtOffset(380, 30);
        contentStream.showText(TAX_NUMBER_OF_COMPANY);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
        contentStream.newLineAtOffset(180, 20);
        contentStream.showText(ZIP_CITY_OF_COMPANY);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
        contentStream.newLineAtOffset(380, 20);
        contentStream.showText(VAT_ID_NUMBER_OF_COMPANY);
        contentStream.endText();

    }

    private void setCustomerAddress(PDPageContentStream contentStream, InvoiceRecord invoiceRecord) throws IOException {
        int y = 800;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText(getFullname(invoiceRecord.getCustomer()));
        contentStream.endText();
        y -= 15;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText(invoiceRecord.getCustomer().adrline1());
        contentStream.endText();
        y -= 15;

        if (invoiceRecord.getCustomer().adrline2() != null && !invoiceRecord.getCustomer().adrline2().isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
            contentStream.newLineAtOffset(30, y);
            contentStream.showText(invoiceRecord.getCustomer().adrline2());
            contentStream.endText();
            y -= 15;
        }

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText(invoiceRecord.getCustomer().postalcode() + " " + invoiceRecord.getCustomer().city());
        contentStream.endText();
    }

    private void setInvoiceHeader(PDPageContentStream contentStream, InvoiceRecord invoiceRecord) throws IOException {
        int y = 710;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Kundennummer");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(150, y);
        contentStream.showText(invoiceRecord.getCustomer().customerNumber());
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Rechnungsnummer");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(150, y);
        contentStream.showText(invoiceRecord.getInvoiceNoAsText());
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Rechnungsdatum");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(150, y);
        contentStream.showText(DateTimeFormatter
                .ofPattern(DATE_FORMAT)
                .withZone(ZoneId.systemDefault())
                .format(invoiceRecord.getInvoiceDate().toInstant()));
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Verwendungszweck");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(150, y);
        contentStream.showText(LITERAL_RECHNUNG + invoiceRecord.getInvoiceNoAsText());
        contentStream.endText();

    }

    private void setInvoiceIntroduction(PDPageContentStream contentStream, InvoiceRecord invoiceRecord) throws IOException {
        int y = 600;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText(String.format("Sehr geehrte(r) %s,", Customer.getFullname(invoiceRecord.getCustomer())));
        contentStream.endText();
        y -= 30;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText(String.format("die genannten Leistungen wurden am %s erbracht.",
                DateTimeFormatter
                        .ofPattern(DATE_FORMAT)
                        .withZone(ZoneId.systemDefault())
                        .format(invoiceRecord.getCustomer().activationDate().toInstant())));
        contentStream.endText();

        y -= 30;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Ihre gekauften Produkte:");
        contentStream.endText();
    }

    private void setInvoicePositions(PDPageContentStream contentStream, InvoiceRecord invoiceRecord) throws IOException {
        int y = 510;
        PDType1Font font;
        String text;
        float textWidth;

        for (CustomerProduct customerProduct : invoiceRecord.getCustomerInvoiceData().products()) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD), 12);
            contentStream.newLineAtOffset(30, y);
            contentStream.showText(CustomerProduct.getProductTranslated(customerProduct.getProduct()));
            contentStream.endText();

            contentStream.beginText();
            font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
            contentStream.setFont(font, 12);
            text = String.format(FORMAT_TWO_DECIMALS, customerProduct.getNetAmount());
            textWidth = getTextWidth(font, 12, text);
            contentStream.newLineAtOffset(500 - textWidth, y);
            contentStream.showText(text);
            contentStream.endText();

            y -= 20;
        }

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Zwischensumme (Nettobetrag)");
        contentStream.endText();

        contentStream.beginText();
        font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        contentStream.setFont(font, 12);
        text = String.format(FORMAT_TWO_DECIMALS, invoiceRecord.getNetAmount());
        textWidth = getTextWidth(font, 12, text);
        contentStream.newLineAtOffset(500 - textWidth, y);
        contentStream.showText(text);
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText(String.format("MwSt. (%.0f%%)", invoiceRecord.getTaxValue().multiply(BigDecimal.valueOf(100))));
        contentStream.endText();
        contentStream.beginText();
        font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        contentStream.setFont(font, 12);
        text = String.format(FORMAT_TWO_DECIMALS, invoiceRecord.getTaxAmount());
        textWidth = getTextWidth(font, 12, text);
        contentStream.newLineAtOffset(500 - textWidth, y);
        contentStream.showText(text);
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Rechnungsbetrag (Bruttobetrag)");
        contentStream.endText();
        contentStream.beginText();
        font = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
        contentStream.setFont(font, 12);
        text = String.format(FORMAT_TWO_DECIMALS, invoiceRecord.getAmount());
        textWidth = getTextWidth(font, 12, text);
        contentStream.newLineAtOffset(500 - textWidth, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void setBankInformation(PDPageContentStream contentStream, InvoiceRecord invoiceRecord) throws IOException {
        int y = 380;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Bitte überweisen Sie den Rechnungsbetrag an");
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(60, y);
        contentStream.showText("Raiffeisenbank Grävenwiesbach eG");
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(60, y);
        contentStream.showText(NAME_OF_COMPANY);
        contentStream.endText();

        y -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(60, y);
        contentStream.showText("IBAN DE59 5006 9345 0100 0362 77");
        contentStream.endText();

        y -= 30;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText(String.format("Zahlbar bis zum %s ohne Abzug.",
                DateTimeFormatter
                        .ofPattern(DATE_FORMAT)
                        .withZone(ZoneId.systemDefault())
                        .format(invoiceRecord.getInvoiceDueDate().toInstant())));
        contentStream.endText();
    }

    private void setInvoiceFinish(PDPageContentStream contentStream) throws IOException {
        int y = 270;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Falls Sie Fragen zu dieser Rechnung haben, dann wenden Sie sich bitte an support@crmupload.de,");
        contentStream.endText();

        y -= 20;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("geben Sie dabei auch bitte die Rechnungsnummer an.");
        contentStream.endText();

        y -= 40;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Viele Grüße");
        contentStream.endText();

        y -= 20;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("Ihr CRM-Upload-Team");
        contentStream.endText();

        y -= 20;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("support@crmupload.de");
        contentStream.endText();

        y -= 20;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
        contentStream.newLineAtOffset(30, y);
        contentStream.showText("www.crmupload.de");
        contentStream.endText();
    }

    private byte[] toPdfBytes(PDDocument document) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.save(out);
            return out.toByteArray();
        }
    }

    public static float getTextWidth(PDType1Font font, int fontSize,
                                     String text) throws IOException {
        return (font.getStringWidth(text) / 1000.0f) * fontSize;
    }

}
