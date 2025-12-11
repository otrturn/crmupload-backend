package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.DuplicateCheckRequest;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Slf4j
@Repository
@SuppressWarnings("squid:S1192")
public class JdbcDuplicateCheckRepositoryAdapter implements DuplicateCheckRepositoryPort {

    private static final String SEQUENCE_DUPLICATE_CHECK_ID = "app.sequence_duplicate_check";

    private static final String SQL_DUPLICATE_CHECK_ID =
            "SELECT nextval('" + SEQUENCE_DUPLICATE_CHECK_ID + "')";

    private static final String SQL_INSERT_DUPLICATE_CHECK =
            "INSERT INTO app.crm_upload " +
                    "(duplicate_check_id, customer_id, source_system, content, status) " +
                    "VALUES (:duplicateCheckId, :customerId, :sourceSystem, :content, :status)";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcDuplicateCheckRepositoryAdapter(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public long nextDuplicateCheckId() {
        try {
            final Long nextId = jdbcTemplate.queryForObject(
                    SQL_DUPLICATE_CHECK_ID,
                    new MapSqlParameterSource(),
                    Long.class
            );

            final Long nonNullNextId = Objects.requireNonNull(
                    nextId,
                    "Sequence " + SEQUENCE_DUPLICATE_CHECK_ID + " returned null"
            );

            if (log.isDebugEnabled()) {
                log.debug("Generated next duplicateCheck id: {}", nonNullNextId);
            }

            return nonNullNextId;
        } catch (DataAccessException ex) {
            log.error("Failed to obtain next duplicateCheck id from sequence {}", SEQUENCE_DUPLICATE_CHECK_ID, ex);
            throw new IllegalStateException("Could not retrieve next duplicateCheck id", ex);
        }
    }

    @Override
    public void insertDuplicateCheck(DuplicateCheckRequest duplicateCheckRequest) {
        if (duplicateCheckRequest.getContent() == null) {
            throw new IllegalArgumentException("content must not be null");
        }
    }

}
