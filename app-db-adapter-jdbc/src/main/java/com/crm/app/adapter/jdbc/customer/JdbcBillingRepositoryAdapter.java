package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.CustomerInvoiceProductData;
import com.crm.app.dto.CustomerProduct;
import com.crm.app.dto.InvoiceRecord;
import com.crm.app.port.customer.BillingRepositoryPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

@Repository
@Slf4j
public class JdbcBillingRepositoryAdapter implements BillingRepositoryPort {

    private static final String SEQUENCE_INVOICE_ID = "app.sequence_customer_invoice";
    private static final String SQL_INVOICE_ID =
            "SELECT nextval('" + SEQUENCE_INVOICE_ID + "')";

    private static final String LITERAL_INVOICE_ID_CAMELCASE = "invoiceId";
    private static final String LITERAL_INVOICE_NO_CAMELCASE = "invoiceNo";
    private static final String LITERAL_CUSTOMER_ID = "customer_id";
    private static final String LITERAL_CUSTOMER_ID_CAMELCASE = "customerId";
    private static final String LITERAL_PRODUCT = "product";
    private static final String LITERAL_ACTIVATION_DATE = "activation_date";
    private static final String LITERAL_ACTIVATION_DATE_CAMELCASE = "activationDate";
    private static final String LITERAL_INVOICE_META_CAMELCASE = "invoiceMeta";
    private static final String LITERAL_INVOICE_IMAGE_CAMELCASE = "invoiceImage";
    private static final String LITERAL_INVOICE_PDF_NAME_CAMELCASE = "invoicePdfName";
    private static final String LITERAL_TAX_VALUE_CAMELCASE = "taxValue";
    private static final String LITERAL_TAX_AMOUNT_CAMELCASE = "taxAmount";
    private static final String LITERAL_NET_AMOUNT_CAMELCASE = "netAmount";
    private static final String LITERAL_AMOUNT_CAMELCASE = "amount";
    private static final String LITERAL_INVOICE_DATE_CAMELCASE = "invoiceDate";
    private static final String LITERAL_INVOICE_DUE_DATE_CAMELCASE = "invoiceDueDate";

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcBillingRepositoryAdapter(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CustomerInvoiceProductData> getCustomersWithActiveProducts() {
        String sql = """
                SELECT
                  cp.customer_id,
                  cp.product,
                  cp.activation_date
                FROM app.customer_product cp
                JOIN app.customer c
                  ON c.customer_id = cp.customer_id
                 AND c.activation_date IS NOT NULL
                 AND c.billable IS TRUE
                WHERE NOT EXISTS (
                  SELECT 1
                  FROM app.customer_invoice ci
                  WHERE ci.customer_id = cp.customer_id
                    AND COALESCE(ci.invoice_meta->'products', '[]'::jsonb)
                        @> jsonb_build_array(
                               jsonb_build_object('product', upper(cp.product))
                           )
                )
                ORDER BY cp.customer_id, cp.product
                """;

        List<Row> rows = jdbc.query(sql, (rs, rowNum) ->
                new Row(
                        rs.getLong(LITERAL_CUSTOMER_ID),
                        rs.getString(LITERAL_PRODUCT),
                        rs.getTimestamp(LITERAL_ACTIVATION_DATE)
                )
        );
        Map<Long, List<CustomerProduct>> grouped = new LinkedHashMap<>();

        for (Row row : rows) {
            grouped
                    .computeIfAbsent(row.customerId(), k -> new ArrayList<>())
                    .add(new CustomerProduct(
                            row.product(),
                            true,
                            row.activationDate()
                    ));
        }

        // Mapping auf CustomerBillingData
        List<CustomerInvoiceProductData> result = new ArrayList<>();

        for (Map.Entry<Long, List<CustomerProduct>> entry : grouped.entrySet()) {
            result.add(new CustomerInvoiceProductData(
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        return result;
    }

    private record Row(
            Long customerId,
            String product,
            Timestamp activationDate
    ) {
    }

    @Override
    public long nextInvoiceId() {
        try {
            final Long nextId = jdbc.queryForObject(SQL_INVOICE_ID, new MapSqlParameterSource(), Long.class);

            final Long nonNullNextId = Objects.requireNonNull(nextId, "Sequence " + SEQUENCE_INVOICE_ID + " returned null");

            if (log.isDebugEnabled()) {
                log.debug(String.format("Generated next invoiceId id: %d", nonNullNextId));
            }

            return nonNullNextId;
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to obtain next invoiceId id from sequence %s", SEQUENCE_INVOICE_ID), ex);
            throw new IllegalStateException("Could not retrieve next invoiceId id", ex);
        }
    }

    @Override
    public void insertInvoiceRecord(InvoiceRecord invoiceRecord) {

        List<CustomerProduct> products = invoiceRecord.getCustomerInvoiceProductData().products().stream()
                .filter(p -> p != null && p.getProduct() != null && !p.getProduct().isBlank())
                .map(p -> {
                    CustomerProduct copy = new CustomerProduct(
                            p.getProduct().trim().toUpperCase(),
                            p.isEnabled(),
                            p.getActivationDate()
                    );
                    copy.setTaxValue(p.getTaxValue());
                    copy.setTaxAmount(p.getTaxAmount());
                    copy.setNetAmount(p.getNetAmount());
                    copy.setAmount(p.getAmount());
                    return copy;
                })
                .toList();

        String billingMetaJson = toBillingMetaJson(products);

        String sql = """
                INSERT INTO app.customer_invoice (
                    invoice_id,
                    invoice_no,
                    customer_id,
                    invoice_date,
                    invoice_due_date,
                    tax_value,
                    tax_amount,
                    net_amount,
                    amount,
                    invoice_meta,
                    invoice_image,
                    invoice_pdf_name
                )
                VALUES (
                    :invoiceId,
                    :invoiceNo,
                    :customerId,
                    :invoiceDate,
                    :invoiceDueDate,
                    :taxValue,
                    :taxAmount,
                    :netAmount,
                    :amount,
                    :invoiceMeta::jsonb,
                    :invoiceImage,
                    :invoicePdfName
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_INVOICE_ID_CAMELCASE, invoiceRecord.getInvoiceId())
                .addValue(LITERAL_INVOICE_NO_CAMELCASE, invoiceRecord.getInvoiceNo())
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, invoiceRecord.getCustomerInvoiceProductData().customerId())
                .addValue(LITERAL_INVOICE_DATE_CAMELCASE, invoiceRecord.getInvoiceDate())
                .addValue(LITERAL_INVOICE_DUE_DATE_CAMELCASE, invoiceRecord.getInvoiceDueDate())
                .addValue(LITERAL_TAX_VALUE_CAMELCASE, invoiceRecord.getTaxValue())
                .addValue(LITERAL_TAX_AMOUNT_CAMELCASE, invoiceRecord.getTaxAmount())
                .addValue(LITERAL_NET_AMOUNT_CAMELCASE, invoiceRecord.getNetAmount())
                .addValue(LITERAL_AMOUNT_CAMELCASE, invoiceRecord.getAmount())
                .addValue(LITERAL_INVOICE_META_CAMELCASE, billingMetaJson, Types.OTHER)
                .addValue(LITERAL_INVOICE_IMAGE_CAMELCASE, invoiceRecord.getInvoiceImage())
                .addValue(LITERAL_INVOICE_PDF_NAME_CAMELCASE, invoiceRecord.getInvoicePdfName());

        jdbc.update(sql, params);
    }

    private String toBillingMetaJson(List<CustomerProduct> products) {
        Map<String, Object> meta = new LinkedHashMap<>();

        List<Map<String, Object>> productObjects = products.stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put(LITERAL_PRODUCT, p.getProduct());                 // z.B. "CRM-UPLOAD"
                    m.put(LITERAL_ACTIVATION_DATE_CAMELCASE, p.getActivationDate());   // Timestamp -> ISO via Jackson
                    m.put(LITERAL_TAX_VALUE_CAMELCASE, p.getTaxValue());
                    m.put(LITERAL_TAX_AMOUNT_CAMELCASE, p.getTaxAmount());
                    m.put(LITERAL_NET_AMOUNT_CAMELCASE, p.getNetAmount());
                    m.put(LITERAL_AMOUNT_CAMELCASE, p.getAmount());
                    return m;
                })
                .toList();

        meta.put("products", productObjects);

        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize invoice_meta JSON", e);
        }
    }

    public List<InvoiceRecord> findInvoicesToBeMailed() {

        String sql = """
                SELECT
                    ci.invoice_id,
                    ci.invoice_no,
                    ci.invoice_date,
                    ci.invoice_due_date,
                    ci.tax_value,
                    ci.tax_amount,
                    ci.net_amount,
                    ci.amount,
                    ci.invoice_image,
                    c.customer_id,
                    c.firstname,
                    c.lastname,
                    c.email_address
                FROM app.customer_invoice ci
                JOIN app.customer c
                  ON c.customer_id = ci.customer_id
                WHERE ci.invoice_mailing_date IS NULL
                  AND ci.cancelled = false
                ORDER BY ci.invoice_date
                """;

        return jdbc.query(sql, new InvoiceRecordRowMapper());
    }

    @Override
    public void setInvoiceToMailed(long invoiceId) {
        String sql = """
                UPDATE app.customer_invoice
                SET invoice_mailing_date = now(),
                    modified = now()
                WHERE invoice_id = :invoiceId
                  AND invoice_mailing_date IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_INVOICE_ID_CAMELCASE, invoiceId);

        int updated = jdbc.update(sql, params);

        if (updated == 0) {
            throw new IllegalStateException(
                    "Invoice not updated (not found or already mailed): invoiceId=" + invoiceId
            );
        }
    }
}
