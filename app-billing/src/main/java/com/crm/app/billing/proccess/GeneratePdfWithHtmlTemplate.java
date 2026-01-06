package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import com.crm.app.billing.error.BillingGeneratePDFException;
import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerProduct;
import com.crm.app.dto.InvoiceRecord;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class GeneratePdfWithHtmlTemplate {

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    private static final String NAME_OF_COMPANY = "Ralf Scholler";
    private static final String STREET_OF_COMPANY = "Am Dorfplatz 6";
    private static final String ZIP_CITY_OF_COMPANY = "57610 Ingelbach";
    private static final String TAX_NUMBER_OF_COMPANY = "Steuernummer 02/227/05080";
    private static final String VAT_ID_NUMBER_OF_COMPANY = "DE 244 3344 16";

    private static final String SUPPORT_EMAIL = "support@crmupload.de";
    private static final String WEBSITE = "www.crmupload.de";

    private static final String BANK_NAME = "Raiffeisenbank Grävenwiesbach eG";
    private static final String BANK_ACCOUNT_NAME = "Ralf Scholler";
    private static final String BANK_IBAN = "DE59 5006 9345 0100 0362 77";

    private final AppBillingConfig appBillingConfig;
    private final TemplateEngine templateEngine;

    public GeneratePdfWithHtmlTemplate(AppBillingConfig appBillingConfig, TemplateEngine templateEngine) {
        this.appBillingConfig = appBillingConfig;
        this.templateEngine = templateEngine;
    }

    /**
     * Rendert invoice.html via Thymeleaf und erzeugt daraus ein PDF (byte[]),
     * kompatibel zu deinem bisherigen Ablauf (Speichern im workdir + Rückgabe byte[]).
     * <p>
     * Voraussetzungen:
     * - src/main/resources/templates/invoice.html
     * - src/main/resources/css/invoice.css (wird über baseUrl eingebunden)
     * - src/main/resources/fonts/DejaVuSans.ttf
     * - src/main/resources/fonts/DejaVuSans-Bold.ttf
     */
    public byte[] generatePDFForCustomer(InvoiceRecord invoiceRecord) throws BillingGeneratePDFException {
        try {
            Map<String, Object> model = buildModel(invoiceRecord);

            Context ctx = new Context(Locale.GERMANY);
            ctx.setVariables(model);

            String html = templateEngine.process("invoice.html", ctx);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();

                // ------------------------------------------------------------
                // FONT EMBEDDING (revisionssicher + entfernt PDFBox "Symbol"-Fallback)
                // ------------------------------------------------------------
                // DejaVu Sans deckt Umlaute + € zuverlässig ab und wird eingebettet.
                builder.useFont(() -> resource("/fonts/DejaVuSans.ttf"),
                        "DejaVu Sans", 400, BaseRendererBuilder.FontStyle.NORMAL, true);

                builder.useFont(() -> resource("/fonts/DejaVuSans-Bold.ttf"),
                        "DejaVu Sans", 700, BaseRendererBuilder.FontStyle.NORMAL, true);

                // WICHTIG: Base-14 "Symbol" auf echten TTF-Font mappen
                // -> verhindert: "Using fallback font LiberationSans for base font Symbol"
                builder.useFont(() -> resource("/fonts/DejaVuSans.ttf"),
                        "Symbol", 400, BaseRendererBuilder.FontStyle.NORMAL, true);

                // Optional (falls irgendwo "ZapfDingbats" auftaucht, gleiche Idee)
                // builder.useFont(() -> resource("/fonts/DejaVuSans.ttf"),

                // >>> NEU: PDF/A-3 aktivieren
                builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_3_U);

                // Color schema#
                byte[] colorProfile;
                try (InputStream is = resource("/color/sRGB.icc")) {
                    colorProfile = is.readAllBytes();
                }
                builder.useColorProfile(colorProfile);

                // ------------------------------------------------------------
                // CSS base URL: Ordner, in dem invoice.css liegt
                // ------------------------------------------------------------
                URL cssBase = getClass().getResource("/css/");
                if (cssBase == null) {
                    throw new IllegalStateException("CSS base URL not found: /css/ (expected under src/main/resources/css/)");
                }

                builder.withHtmlContent(html, cssBase.toExternalForm());
                builder.toStream(out);

                builder.run();

                byte[] pdfBytes = out.toByteArray();

                String pdfName = String.format("Rechnung_%06d.pdf", invoiceRecord.getInvoiceId());
                Path pdfPath = Path.of(appBillingConfig.getInvoiceDir(), pdfName);
                invoiceRecord.setInvoicePdfName(pdfPath.toString());
                Files.write(pdfPath, pdfBytes);

                return pdfBytes;
            }
        } catch (Exception e) {
            log.error("generatePDFForCustomer (HTML Template)", e);
            throw new BillingGeneratePDFException("generatePDFForCustomer (HTML Template)", e);
        }
    }

    private static InputStream resource(String classpathLocation) {
        InputStream in = GeneratePdfWithHtmlTemplate.class.getResourceAsStream(classpathLocation);
        if (in == null) {
            throw new IllegalStateException("Font resource not found on classpath: " + classpathLocation);
        }
        return in;
    }

    private Map<String, Object> buildModel(InvoiceRecord invoiceRecord) {

        DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.systemDefault());

        // -------- issuer --------
        Map<String, Object> issuer = new LinkedHashMap<>();
        issuer.put("name", NAME_OF_COMPANY);
        issuer.put("street", STREET_OF_COMPANY);
        issuer.put("zipCity", ZIP_CITY_OF_COMPANY);
        issuer.put("taxNumber", TAX_NUMBER_OF_COMPANY);
        issuer.put("vatId", VAT_ID_NUMBER_OF_COMPANY);
        issuer.put("supportEmail", SUPPORT_EMAIL);
        issuer.put("website", WEBSITE);

        // -------- customer --------
        Customer c = invoiceRecord.getCustomer();
        String fullName = Customer.getFullname(c);
        String street = (c.adrline1() == null ? "" : c.adrline1());
        if (c.adrline2() != null && !c.adrline2().isBlank()) {
            street = street + " " + c.adrline2();
        }
        String zipCity = (c.postalcode() == null ? "" : c.postalcode()) + " " + (c.city() == null ? "" : c.city());

        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("fullName", fullName);
        customer.put("street", street.trim());
        customer.put("zipCity", zipCity.trim());

        // -------- invoice --------
        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("customerNumber", c.customerNumber());
        invoice.put("invoiceNo", invoiceRecord.getInvoiceNo());
        invoice.put("invoiceDate", df.format(invoiceRecord.getInvoiceDate().toInstant()));
        invoice.put("serviceDate", df.format(c.activationDate().toInstant()));
        invoice.put("dueDate", df.format(invoiceRecord.getInvoiceDueDate().toInstant()));

        // -------- items --------
        List<Map<String, Object>> items = invoiceRecord.getCustomerInvoiceProductData().products().stream()
                .filter(Objects::nonNull)
                .map(p -> {
                    Map<String, Object> it = new LinkedHashMap<>();
                    it.put("name", CustomerProduct.getProductTranslated(p.getProduct()));
                    it.put("priceFormatted", formatEuro(p.getNetAmount()));
                    return it;
                })
                .toList();

        // -------- totals --------
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("netFormatted", formatEuro(invoiceRecord.getNetAmount()));
        totals.put("taxFormatted", formatEuro(invoiceRecord.getTaxAmount()));
        totals.put("grossFormatted", formatEuro(invoiceRecord.getAmount()));

        BigDecimal taxRatePercent = invoiceRecord.getTaxValue() == null
                ? BigDecimal.ZERO
                : invoiceRecord.getTaxValue().multiply(BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP);
        totals.put("taxRatePercent", taxRatePercent);

        // -------- bank --------
        Map<String, Object> bank = new LinkedHashMap<>();
        bank.put("name", BANK_NAME);
        bank.put("accountName", BANK_ACCOUNT_NAME);
        bank.put("iban", BANK_IBAN);

        // -------- root model --------
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("issuer", issuer);
        model.put("customer", customer);
        model.put("invoice", invoice);
        model.put("items", items);
        model.put("totals", totals);
        model.put("bank", bank);

        return model;
    }

    private String formatEuro(BigDecimal value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return nf.format(value == null ? BigDecimal.ZERO : value);
    }
}
