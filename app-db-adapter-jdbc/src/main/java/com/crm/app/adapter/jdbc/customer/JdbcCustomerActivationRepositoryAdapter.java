package com.crm.app.adapter.jdbc.customer;

import com.crm.app.port.customer.CustomerActivationRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class JdbcCustomerActivationRepositoryAdapter implements CustomerActivationRepositoryPort {

    private static final String SQL_INSERT_TOKEN = """
            INSERT INTO app.customer_activation (token, customer_id, expires_at)
            VALUES (:token, :customerId, now() + interval '24 hours')
            """;

    private static final String SQL_FIND_VALID_TOKEN = """
            SELECT customer_id
              FROM app.customer_activation
             WHERE token = :token
               AND used = FALSE
               AND expires_at > now()
            """;

    private static final String SQL_MARK_TOKEN_USED = """
            UPDATE app.customer_activation
               SET used = TRUE,
                   used_at = now()
             WHERE token = :token
            """;

    private static final String LITERAL_TOKEN = "token";
    private static final String LITERAL_CUSTOMER_ID_CAMELCASE = "customerId";
    private static final String LITERAL_CUSTOMER_ID = "customer_id";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcCustomerActivationRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public String createActivationToken(long customerId) {
        UUID token = UUID.randomUUID();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_TOKEN, token)
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customerId);

        try {
            jdbcTemplate.update(SQL_INSERT_TOKEN, params);
            log.info("Created activation token for customerId={}", customerId);
            return token.toString();
        } catch (DataAccessException ex) {
            log.error("Failed to create activation token for customerId={}", customerId, ex);
            throw new IllegalStateException("Could not create activation token", ex);
        }
    }

    @Override
    public Optional<Long> findValidCustomerIdByToken(String token) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_TOKEN, UUID.fromString(token));

        try {
            return jdbcTemplate.query(SQL_FIND_VALID_TOKEN, params, rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getLong(LITERAL_CUSTOMER_ID));
            });
        } catch (DataAccessException ex) {
            log.error("Failed to load activation token {}", token, ex);
            throw new IllegalStateException("Could not load activation token", ex);
        }
    }

    @Override
    public void markTokenUsed(String token) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_TOKEN, UUID.fromString(token));
        try {
            jdbcTemplate.update(SQL_MARK_TOKEN_USED, params);
        } catch (DataAccessException ex) {
            log.error("Failed to mark activation token {} as used", token, ex);
            throw new IllegalStateException("Could not update activation token", ex);
        }
    }
}
