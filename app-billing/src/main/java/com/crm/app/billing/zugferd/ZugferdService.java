package com.crm.app.billing.zugferd;

import com.crm.app.billing.error.BillingZUGFeRDException;
import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerInvoiceProductData;
import com.crm.app.dto.CustomerProduct;
import com.crm.app.dto.InvoiceRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mustangproject.*;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.Profiles;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/*
 * Drop-in Service: takes your already-created PDF/A-3 (referenced via invoiceRecord.invoicePdfName in /data/invoices),
 * generates the ZUGFeRD/Factur-X XML from Mustang Invoice/Item/Product Java objects, embeds it into the PDF,
 * and returns the resulting PDF bytes (for storing into invoice_image).
 *
 * Requires Mustang dependency: org.mustangproject:library
 *
 * Notes:
 * - Input is expected to be a valid PDF/A (PDF/A-3 in your case).
 * - This implementation exports to a file (robust) and then reads bytes back.
 */
@Slf4j
@RequiredArgsConstructor
public class ZugferdService {

    /**
     * Directory where your PDF files live, e.g. Path.of("/data/invoices")
     */
    private final Path invoicesDir;

    /**
     * Seller/issuer master data (from config)
     */
    private final IssuerData issuer;

    /**
     * Producer/creator metadata (optional)
     */
    private final String producer;
    private final String creator;

    /**
     * Enriches the PDF referenced by invoiceRecord.invoicePdfName with ZUGFeRD (EN16931 profile)
     * and returns the resulting PDF as byte[] (ready for DB).
     *
     * @param invoiceRecord must contain customer + product data + invoicePdfName + invoiceNo + dates
     */
    public byte[] createZugferdPdfBytes(InvoiceRecord invoiceRecord) {
        Objects.requireNonNull(invoiceRecord, "invoiceRecord");
        if (invoiceRecord.getInvoicePdfName() == null || invoiceRecord.getInvoicePdfName().isBlank()) {
            throw new IllegalArgumentException("invoicePdfName is null/blank for invoice " + invoiceRecord.getInvoiceNo());
        }
        if (invoiceRecord.getCustomer() == null) {
            throw new IllegalArgumentException("Customer is null for invoice " + invoiceRecord.getInvoiceNo());
        }
        if (invoiceRecord.getCustomerInvoiceProductData() == null) {
            throw new IllegalArgumentException("CustomerInvoiceProductData is null for invoice " + invoiceRecord.getInvoiceNo());
        }

        Path sourcePdf = invoicesDir.resolve(invoiceRecord.getInvoicePdfName());

        // You can overwrite the same filename if you want; I keep a suffix to ease debugging.
        String outName = withSuffix(invoiceRecord.getInvoicePdfName());
        Path targetPdf = invoicesDir.resolve(outName);

        ensureReadable(sourcePdf);

        Invoice mustangInvoice = buildMustangInvoice(invoiceRecord);

        try (ZUGFeRDExporterFromPDFA ze = new ZUGFeRDExporterFromPDFA()) {

            ze.load(sourcePdf.toString());
            ze.setProducer(producer != null ? producer : "crmupload.de");
            ze.setCreator(creator != null ? creator : System.getProperty("user.name"));

            // EN16931 is the usual profile for B2B e-invoices (ZUGFeRD/Factur-X).
            ze.setProfile(Profiles.getByName("EN16931"));
            ze.setTransaction(mustangInvoice);

            ze.export(targetPdf.toString());

            byte[] bytes = Files.readAllBytes(targetPdf);

            log.info("ZUGFeRD PDF created: invoiceNo={}, source={}, target={}, bytes={}",
                    invoiceRecord.getInvoiceNo(), sourcePdf, targetPdf, bytes.length);

            return bytes;

        } catch (Exception e) {
            throw new BillingZUGFeRDException(
                    "Failed to create ZUGFeRD PDF for invoice " + invoiceRecord.getInvoiceNo(), e);
        }

    }

    // --------------------------------------------------------------------------------------------
    // Build Mustang Invoice from your DTOs
    // --------------------------------------------------------------------------------------------

    private Invoice buildMustangInvoice(InvoiceRecord r) {
        Customer customer = r.getCustomer();
        CustomerInvoiceProductData pid = r.getCustomerInvoiceProductData();

        Date issueDate = toDateOrThrow(r.getInvoiceDate(), "invoiceDate");
        Date dueDate = toDateOrThrow(r.getInvoiceDueDate(), "invoiceDueDate");

        // If you don’t have a dedicated delivery/performance date yet, use issue date as pragmatic default.

        TradeParty sender = buildSenderTradeParty(issuer);
        TradeParty recipient = buildRecipientTradeParty(customer);

        Invoice invoice = new Invoice()
                .setNumber(nullToEmpty(r.getInvoiceNo()))
                .setIssueDate(issueDate)
                .setDueDate(dueDate)
                .setDeliveryDate(issueDate)
                .setSender(sender)
                .setRecipient(recipient);

        List<CustomerProduct> products = pid.products();
        if (products == null || products.isEmpty()) {
            throw new IllegalStateException("No products present for invoice " + r.getInvoiceNo());
        }

        int added = 0;
        for (CustomerProduct p : products) {
            if (!p.isEnabled()) {
                continue;
            }
            invoice.addItem(mapProductToItem(p));
            added++;
        }

        if (added == 0) {
            throw new IllegalStateException("All products disabled; nothing to invoice for invoice " + r.getInvoiceNo());
        }

        return invoice;
    }

    private IZUGFeRDExportableItem mapProductToItem(CustomerProduct p) {
        String translated = CustomerProduct.getProductTranslated(p.getProduct());
        if (translated == null || translated.isBlank()) {
            // Fallback to raw product key to avoid empty descriptions in XML
            translated = nullToEmpty(p.getProduct());
        }

        BigDecimal vatPercent = p.getTaxValue();
        BigDecimal net = p.getNetAmount();

        if (vatPercent == null) {
            throw new IllegalStateException("taxValue (VAT percent) missing for product=" + p.getProduct());
        }
        if (net == null) {
            throw new IllegalStateException("netAmount missing for product=" + p.getProduct());
        }

        // Unit "C62" = "piece" (common default for services billed per unit)
        Product prod = new Product(translated, "", "C62", vatPercent);

        // Item(Product product, BigDecimal price, BigDecimal quantity)
        // Price is net unit price; quantity we set to 1.
        return new Item(prod, net, BigDecimal.ONE);
    }

    private TradeParty buildSenderTradeParty(IssuerData i) {
        TradeParty sender = new TradeParty(
                required(i.companyName(), "issuer.companyName"),
                required(i.street(), "issuer.street"),
                required(i.postalCode(), "issuer.postalCode"),
                required(i.city(), "issuer.city"),
                required(i.countryCode(), "issuer.countryCode")
        );

        if (i.email() != null && !i.email().isBlank()) {
            sender.setEmail(i.email());
        }
        if (i.vatId() != null && !i.vatId().isBlank()) {
            sender.addVATID(i.vatId());
        }
        if (i.taxNumber() != null && !i.taxNumber().isBlank()) {
            sender.addTaxID(i.taxNumber());
        }
        if (i.contactName() != null || i.contactPhone() != null || i.contactEmail() != null) {
            sender.setContact(new Contact(
                    nullToEmpty(i.contactName()),
                    nullToEmpty(i.contactPhone()),
                    nullToEmpty(i.contactEmail())
            ));
        }
        if (i.iban() != null && !i.iban().isBlank()) {
            sender.addBankDetails(new BankDetails(
                    i.iban(),
                    nullToEmpty(i.bic())
            ));
        }
        return sender;
    }

    private TradeParty buildRecipientTradeParty(Customer c) {
        String name = Customer.getFullname(c);
        if (name.isBlank()) {
            name = nullToEmpty(c.companyName());
        }
        if (name.isBlank()) {
            name = "Kunde"; // last resort
        }

        TradeParty recipient = new TradeParty(
                name,
                required(c.adrline1(), "customer.adrline1"),
                required(c.postalcode(), "customer.postalcode"),
                required(c.city(), "customer.city"),
                required(c.country(), "customer.country")
        );

        if (c.emailAddress() != null && !c.emailAddress().isBlank()) {
            recipient.setEmail(c.emailAddress());
        }

        return recipient;
    }

    // --------------------------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------------------------

    private static Date toDateOrThrow(Timestamp ts, String field) {
        if (ts == null) throw new IllegalArgumentException(field + " is null");
        // Keep Europe/Berlin semantics (though Date itself is UTC-based)
        return Date.from(ts.toInstant().atZone(ZoneId.of("Europe/Berlin")).toInstant());
    }

    private static void ensureReadable(Path p) {
        if (!Files.exists(p)) {
            throw new IllegalStateException("Source PDF does not exist: " + p);
        }
        if (!Files.isRegularFile(p)) {
            throw new IllegalStateException("Source PDF is not a file: " + p);
        }
        if (!Files.isReadable(p)) {
            throw new IllegalStateException("Source PDF is not readable: " + p);
        }
    }

    private static String withSuffix(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) return filename + "_ZUGFeRD";
        return filename.substring(0, dot) + "_ZUGFeRD" + filename.substring(dot);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required field missing: " + field);
        }
        return value;
    }

    /**
     * Seller master data (put this into Spring @ConfigurationProperties if you want).
     */
    public record IssuerData(
            String companyName,
            String street,
            String postalCode,
            String city,
            String countryCode,   // "DE"
            String taxNumber,     // e.g. "12/345/67890" (optional)
            String vatId,         // e.g. "DE123456789" (optional)
            String email,         // e.g. "support@crmupload.de" (optional)
            String contactName,   // optional
            String contactPhone,  // optional
            String contactEmail,  // optional
            String iban,          // optional (recommended for some profiles/recipients)
            String bic            // optional
    ) {
    }
}
