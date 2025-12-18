package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.CustomerBillingData;
import com.crm.app.dto.CustomerProduct;
import com.crm.app.port.customer.BillingRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;

@Repository
@Slf4j
public class JdbcBillingRepositoryAdapter implements BillingRepositoryPort {

    private static final String LITERAL_CUSTOMER_ID = "customer_id";
    private static final String LITERAL_CUSTOMER_ID_CAMELCASE = "customerId";
    private static final String LITERAL_PRODUCT = "product";
    private static final String LITERAL_ACTIVATION_DATE = "activation_date";

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcBillingRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
                        ? upper(cp.product)
                )
                ORDER BY cp.customer_id, cp.product
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();

        List<Row> rows = jdbc.query(sql, params, (rs, rowNum) ->
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
}
