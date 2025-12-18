package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerBillingData;
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

    private static final String SEQUENCE_INVOICE_NO = "app.sequence_customer_billing";
    private static final String SQL_INVOICE_NO =
            "SELECT nextval('" + SEQUENCE_INVOICE_NO + "')";

    private static final String LITERAL_CUSTOMER_ID = "customer_id";
    private static final String LITERAL_CUSTOMER_ID_CAMELCASE = "customerId";
    private static final String LITERAL_PRODUCT = "product";
    private static final String LITERAL_ACTIVATION_DATE = "activation_date";
    private static final String LITERAL_INVOICE_NO_CAMELCASE = "invoiceNo";
    private static final String LITERAL_BILLING_META_CAMELCASE = "billingMeta";
    private static final String LITERAL_INVOICE_IMAGE_CAMELCASE = "invoiceImage";
    private static final String LITERAL_TAX_VALUE_CAMELCASE = "taxValue";
    private static final String LITERAL_TAX_AMOUNT_CAMELCASE = "taxAmount";
    private static final String LITERAL_NET_AMOUNT_CAMELCASE = "netAmount";
    private static final String LITERAL_AMOUNT_CAMELCASE = "amount";

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcBillingRepositoryAdapter(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CustomerBillingData> getCustomerProductsByCustomerId(long customerId) {

        String sql = """
                SELECT
                  cp.customer_id,
                  cp.product,
                  cp.activation_date
                FROM app.customer_product cp
                WHERE cp.customer_id = :customerId
                  AND NOT EXISTS (
                    SELECT 1
                    FROM app.customer_billing cb
                    WHERE cb.customer_id = cp.customer_id
                      AND COALESCE(cb.billing_meta->'products', '[]'::jsonb)
                          ? upper(cp.product)
                  )
                ORDER BY cp.product
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customerId);

        List<Row> rows = jdbc.query(sql, params, (rs, rowNum) ->
                new Row(
                        rs.getLong(LITERAL_CUSTOMER_ID),
                        rs.getString(LITERAL_PRODUCT),
                        rs.getTimestamp(LITERAL_ACTIVATION_DATE)
                )
        );

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Long cid = rows.get(0).customerId();
        List<CustomerProduct> products = new ArrayList<>();

        for (Row r : rows) {
            products.add(new CustomerProduct(
                    r.product(),
                    r.activationDate()
            ));
        }

        return Optional.of(new CustomerBillingData(cid, products));
    }

    @Override
    public List<CustomerBillingData> getCustomersWithProducts() {
        String sql = """
                SELECT
                  cp.customer_id,
                  cp.product,
                  cp.activation_date
                FROM app.customer_product cp
                WHERE NOT EXISTS (
                  SELECT 1
                  FROM app.customer_billing cb
                  WHERE cb.customer_id = cp.customer_id
                    AND COALESCE(cb.billing_meta->'products', '[]'::jsonb)
                        @> to_jsonb(ARRAY[upper(cp.product)])::jsonb
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
                            row.activationDate()
                    ));
        }

        // Mapping auf CustomerBillingData
        List<CustomerBillingData> result = new ArrayList<>();

        for (Map.Entry<Long, List<CustomerProduct>> entry : grouped.entrySet()) {
            result.add(new CustomerBillingData(
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
    public long nextInvoiceNo() {
        try {
            final Long nextId = jdbc.queryForObject(SQL_INVOICE_NO, new MapSqlParameterSource(), Long.class);

            final Long nonNullNextId = Objects.requireNonNull(nextId, "Sequence " + SEQUENCE_INVOICE_NO + " returned null");

            if (log.isDebugEnabled()) {
                log.debug(String.format("Generated next invoiceNo id: %d", nonNullNextId));
            }

            return nonNullNextId;
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to obtain next invoiceNo id from sequence %s", SEQUENCE_INVOICE_NO), ex);
            throw new IllegalStateException("Could not retrieve next invoiceNo id", ex);
        }
    }

    @Override
    public void insertInvoiceRecord( InvoiceRecord invoiceRecord) {
        List<String> productCodes = invoiceRecord.getCustomerBillingData().products().stream()
                .map(CustomerProduct::product)
                .filter(p -> p != null && !p.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .toList();

        String billingMetaJson = toBillingMetaJson(productCodes);

        String sql = """
                    INSERT INTO app.customer_billing (
                        customer_id,
                        invoice_no,
                        tax_value,
                        tax_amount,
                        net_amount,
                        amount,
                        billing_meta,
                        invoice_image,
                        submitted_to_billing
                    )
                    VALUES (
                        :customerId,
                        :invoiceNo,
                        :taxValue,
                        :taxAmount,
                        :netAmount,
                        :amount,
                        :billingMeta::jsonb,
                        :invoiceImage,
                        NULL
                    )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, invoiceRecord.getCustomerBillingData().customerId())
                .addValue(LITERAL_INVOICE_NO_CAMELCASE, invoiceRecord.getInvoiceNo())
                .addValue(LITERAL_TAX_VALUE_CAMELCASE, invoiceRecord.getTaxValue())
                .addValue(LITERAL_TAX_AMOUNT_CAMELCASE, invoiceRecord.getTaxAmount())
                .addValue(LITERAL_NET_AMOUNT_CAMELCASE, invoiceRecord.getNetAmount())
                .addValue(LITERAL_AMOUNT_CAMELCASE, invoiceRecord.getAmount())
                .addValue(LITERAL_BILLING_META_CAMELCASE, billingMetaJson, Types.OTHER)
                .addValue(LITERAL_INVOICE_IMAGE_CAMELCASE, invoiceRecord.getInvoiceImage());

        jdbc.update(sql, params);
    }

    private String toBillingMetaJson(List<String> productsUpper) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("products", productsUpper);
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize billing_meta JSON", e);
        }
    }
}
