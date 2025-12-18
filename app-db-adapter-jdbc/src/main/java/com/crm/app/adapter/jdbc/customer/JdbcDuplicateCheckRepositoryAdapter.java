package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckRequest;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@SuppressWarnings("squid:S1192")
public class JdbcDuplicateCheckRepositoryAdapter implements DuplicateCheckRepositoryPort {

    private static final String SEQUENCE_DUPLICATE_CHECK_ID = "app.sequence_duplicate_check";

    private static final String SQL_SEQUENCE_DUPLICATE_CHECK_ID =
            "SELECT nextval('" + SEQUENCE_DUPLICATE_CHECK_ID + "')";

    private static final String SQL_INSERT_DUPLICATE_CHECK =
            "INSERT INTO app.duplicate_check " +
                    "(duplicate_check_id, customer_id, source_system, content, status) " +
                    "VALUES (:duplicateCheckId, :customerId, :sourceSystem, :content, :status)";

    private static final String SQL_INSERT_DUPLICATE_CHECK_OBSERVATION =
            "INSERT INTO app.duplicate_check_observation " +
                    "(duplicate_check_id, customer_id, source_system, content, status) " +
                    "VALUES (:duplicateCheckId, :customerId, :sourceSystem, :content, :status)";

    private static final String SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_VERIFICATION = """
            UPDATE app.duplicate_check dc
               SET status = 'verifying'
             WHERE dc.duplicate_check_id IN (
                   SELECT duplicate_check_id
                     FROM app.duplicate_check
                    WHERE status = 'new'
                    ORDER BY duplicate_check_id
                    FOR UPDATE SKIP LOCKED
                    LIMIT :limit
               )
             RETURNING duplicate_check_id
            """;

    private static final String SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_FINALISATION = """
            UPDATE app.duplicate_check dc
               SET status = 'finalising'
             WHERE dc.duplicate_check_id IN (
                   SELECT duplicate_check_id
                     FROM app.duplicate_check
                    WHERE status = 'duplicate-checked'
                    ORDER BY duplicate_check_id
                    FOR UPDATE SKIP LOCKED
                    LIMIT :limit
               )
             RETURNING duplicate_check_id
            """;

    private static final String SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_CHECK = """
            UPDATE app.duplicate_check dc
               SET status = 'duplicate-checking'
             WHERE dc.duplicate_check_id IN (
                   SELECT duplicate_check_id
                     FROM app.duplicate_check
                    WHERE status = 'verified'
                    ORDER BY duplicate_check_id
                    FOR UPDATE SKIP LOCKED
                    LIMIT :limit
               )
             RETURNING duplicate_check_id
            """;

    private static final String SQL_MARK_DUPLICATE_CHECK_VERIFIED = """
            UPDATE app.duplicate_check
               SET status = 'verified',
                   content = :content,
                   modified = now()
             WHERE duplicate_check_id = :duplicateCheckId
            """;

    private static final String SQL_MARK_DUPLICATE_CHECK_CHECKED = """
            UPDATE app.duplicate_check
               SET status = 'duplicate-checked',
                   content = :content,
                   modified = now()
             WHERE duplicate_check_id = :duplicateCheckId
            """;

    private static final String SQL_MARK_DUPLICATE_CHECK_FAILED = """
            UPDATE app.duplicate_check
               SET status = 'failed',
                   content = NULL,
                   last_error = :error,
                   modified = now()
             WHERE duplicate_check_id = :duplicateCheckId
            """;

    private static final String SQL_MARK_DUPLICATE_CHECK_FAILED_KEEP_CONTENT = """
            UPDATE app.duplicate_check
               SET status = 'failed',
                   last_error = :error,
                   modified = now()
             WHERE duplicate_check_id = :duplicateCheckId
            """;

    private static final String SQL_MARK_DUPLICATE_CHECK_DONE = """
            UPDATE app.duplicate_check
               SET status = 'done',
                   content = NULL,
                   modified = now()
             WHERE duplicate_check_id = :duplicateCheckId
            """;

    private static final String SQL_MARK_DUPLICATE_CHECK_DONE_KEEP_CONTENT = """
            UPDATE app.duplicate_check
               SET status = 'done',
                   content = NULL,
                   modified = now()
             WHERE duplicate_check_id = :duplicateCheckId
            """;

    private static final String SQL_FIND_DUPLICATE_CHECKS_BY_IDS = """
            SELECT duplicate_check_id,
                   customer_id,
                   source_system,
                   content
              FROM app.duplicate_check
             WHERE duplicate_check_id = ANY(ARRAY[:duplicateCheckIds])
            """;

    private static final String SQL_FIND_UNDER_OBSERVATION_BY_DUPLICATE_CHECK_ID = """
            SELECT
            c.under_observation as under_observation
            FROM app.duplicate_check dc
            JOIN app.customer c
            ON c.customer_id =dc.customer_id
            WHERE dc.duplicate_check_id =:duplicate_check_id
            """;

    private static final String SQL_FIND_UNDER_OBSERVATION_BY_CUSTOMER_ID = """
            SELECT
            c.under_observation as under_observation
            FROM app.customer c
            WHERE c.customer_id =:customer_id
            """;

    private static final String LITERAL_DUPLICATE_CHECK_ID = "duplicate_check_id";
    private static final String LITERAL_CUSTOMER_ID = "customer_id";
    private static final String LITERAL_SOURCE_SYSTEM = "source_system";
    private static final String LITERAL_CONTENT = "content";
    private static final String LITERAL_STATUS = "status";
    private static final String LITERAL_LIMIT = "limit";
    private static final String LITERAL_ERROR = "error";

    private static final String LITERAL_DUPLICATE_CHECK_ID_CAMELCASE = "duplicateCheckId";
    private static final String LITERAL_CUSTOMER_ID_CAMELCASE = "customerId";
    private static final String LITERAL_SOURCE_SYSTEM_CAMELCASE = "sourceSystem";
    private static final String LITERAL_DUPLICATE_CHECK_IDS_CAMELCASE = "duplicateCheckIds";

    private static final String LITERAL_NO_CUSTOMER_FOR_DUPLICATE_CHECK_ID = "No customer found for duplicateCheckId '%d'";
    private static final String LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID = "No customer found for customerId'%d'";

    private static final String STATUS_DUPLICATE_CHECK_NEW = "new";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcDuplicateCheckRepositoryAdapter(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public long nextDuplicateCheckId() {
        try {
            final Long nextId = jdbcTemplate.queryForObject(SQL_SEQUENCE_DUPLICATE_CHECK_ID, new MapSqlParameterSource(), Long.class);

            final Long nonNullNextId = Objects.requireNonNull(nextId, "Sequence " + SEQUENCE_DUPLICATE_CHECK_ID + " returned null");

            if (log.isDebugEnabled()) {
                log.debug(String.format("Generated next duplicateCheck id: %d", nonNullNextId));
            }

            return nonNullNextId;
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to obtain next duplicateCheck id from sequence %s", SEQUENCE_DUPLICATE_CHECK_ID), ex);
            throw new IllegalStateException("Could not retrieve next duplicateCheck id", ex);
        }
    }

    @Override
    public void insertDuplicateCheck(DuplicateCheckRequest duplicateCheckRequest) {
        if (duplicateCheckRequest.getContent() == null) {
            throw new IllegalArgumentException("content must not be null");
        }
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, duplicateCheckRequest.getDuplicateCheckId())
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, duplicateCheckRequest.getCustomerId())
                .addValue(LITERAL_SOURCE_SYSTEM_CAMELCASE, duplicateCheckRequest.getSourceSystem())
                .addValue(LITERAL_CONTENT, duplicateCheckRequest.getContent())
                .addValue(LITERAL_STATUS, STATUS_DUPLICATE_CHECK_NEW);

        try {
            final int affectedRows = jdbcTemplate.update(SQL_INSERT_DUPLICATE_CHECK, params);

            if (affectedRows != 1) {
                log.error(String.format("Insert into app.duplicate_check affected %d rows for duplicateCheckId=%d", affectedRows, duplicateCheckRequest.getDuplicateCheckId()));
                throw new IllegalStateException("Insert into app.duplicate_check did not affect exactly one row (affected=" + affectedRows + ")");
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Inserted duplicate-check: duplicateCheckId=%d, customerId=%d, status=%s", duplicateCheckRequest.getDuplicateCheckId(), duplicateCheckRequest.getCustomerId(), STATUS_DUPLICATE_CHECK_NEW));
            }

            if (isUnderObservationByCustomerId(duplicateCheckRequest.getCustomerId())) {
                jdbcTemplate.update(SQL_INSERT_DUPLICATE_CHECK_OBSERVATION, params);
            }

        } catch (DataAccessException ex) {
            log.error(String.format("Failed to insert duplicate-check upload for duplicateCheckId=%d, customerId=%d", duplicateCheckRequest.getDuplicateCheckId(), duplicateCheckRequest.getCustomerId()), ex);
            throw new IllegalStateException("Could not insert customer upload", ex);
        }
    }

    @Override
    public List<Long> claimNextDuplicateChecksForVerification(final int limit) {
        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_LIMIT, limit);
        try {
            return jdbcTemplate.query(SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_VERIFICATION, params, (rs, rowNum) -> rs.getLong(LITERAL_DUPLICATE_CHECK_ID));
        } catch (DataAccessException ex) {
            log.error("Failed to claim duplicate checks for verification", ex);
            throw new IllegalStateException("Could not claim next duplicate checks for verification", ex);
        }
    }

    @Override
    public List<Long> claimNextDuplicateChecksForFinalisation(final int limit) {
        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_LIMIT, limit);
        try {
            return jdbcTemplate.query(SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_FINALISATION, params, (rs, rowNum) -> rs.getLong(LITERAL_DUPLICATE_CHECK_ID));
        } catch (DataAccessException ex) {
            log.error("Failed to claim duplicate checks for finalisation", ex);
            throw new IllegalStateException("Could not claim next duplicate checks for finalisation", ex);
        }
    }

    @Override
    public List<Long> claimNextDuplicateChecksForCheck(final int limit) {
        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_LIMIT, limit);
        try {
            return jdbcTemplate.query(SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_CHECK, params, (rs, rowNum) -> rs.getLong(LITERAL_DUPLICATE_CHECK_ID));
        } catch (DataAccessException ex) {
            log.error("Failed to claim duplicate checks for finalisation", ex);
            throw new IllegalStateException("Could not claim next duplicate checks for finalisation", ex);
        }
    }

    @Override
    public void markDuplicateCheckVerified(final long duplicateCheckId, byte[] content) {
        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, duplicateCheckId).addValue(LITERAL_CONTENT, content);
        try {
            jdbcTemplate.update(SQL_MARK_DUPLICATE_CHECK_VERIFIED, params);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to mark duplicate check %d as verified", duplicateCheckId), ex);
            throw new IllegalStateException("Could not mark duplicate check as checked", ex);
        }
    }

    @Override
    public void markDuplicateCheckChecked(final long duplicateCheckId, byte[] content) {
        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, duplicateCheckId).addValue(LITERAL_CONTENT, content);
        try {
            jdbcTemplate.update(SQL_MARK_DUPLICATE_CHECK_CHECKED, params);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to mark duplicate check %d as checked", duplicateCheckId), ex);
            throw new IllegalStateException("Could not mark duplicate check as checked", ex);
        }
    }

    @Override
    public void markDuplicateCheckFailed(final long duplicateCheckId, final String errorMessage) {
        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, duplicateCheckId).addValue(LITERAL_ERROR, errorMessage);
        try {
            if (isUnderObservationByDuplicateCheckId(duplicateCheckId)) {
                jdbcTemplate.update(SQL_MARK_DUPLICATE_CHECK_FAILED_KEEP_CONTENT, params);
            } else {
                jdbcTemplate.update(SQL_MARK_DUPLICATE_CHECK_FAILED, params);
            }
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to mark duplicate check %d as failed", duplicateCheckId), ex);
            throw new IllegalStateException("Could not mark duplicate check as failed", ex);
        }
    }

    @Override
    public void markDuplicateCheckDone(final long duplicateCheckId) {
        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, duplicateCheckId);
        try {
            if (isUnderObservationByDuplicateCheckId(duplicateCheckId)) {
                jdbcTemplate.update(SQL_MARK_DUPLICATE_CHECK_DONE_KEEP_CONTENT, params);
            } else {
                jdbcTemplate.update(SQL_MARK_DUPLICATE_CHECK_DONE, params);
            }
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to mark duplicate check %d as done", duplicateCheckId), ex);
            throw new IllegalStateException("Could not mark duplicate check as done", ex);
        }
    }

    @Override
    public List<DuplicateCheckContent> findDuplicateChecksByIds(List<Long> duplicateCheckIds) {
        if (duplicateCheckIds == null || duplicateCheckIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_DUPLICATE_CHECK_IDS_CAMELCASE, duplicateCheckIds);

        try {
            return jdbcTemplate.query(
                    SQL_FIND_DUPLICATE_CHECKS_BY_IDS,
                    params,
                    (rs, rowNum) -> new DuplicateCheckContent(
                            rs.getLong(LITERAL_DUPLICATE_CHECK_ID),
                            rs.getLong(LITERAL_CUSTOMER_ID),
                            rs.getString(LITERAL_SOURCE_SYSTEM),
                            rs.getBytes(LITERAL_CONTENT)
                    )
            );
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to load duplicate-check for ids=%s", String.valueOf(duplicateCheckIds)), ex);
            throw new IllegalStateException("Could not load customer duplicate-check", ex);
        }
    }

    @Override
    public boolean isUnderObservationByDuplicateCheckId(long duplicateCheckId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_DUPLICATE_CHECK_ID, duplicateCheckId);

        try {
            Boolean underObservation = jdbcTemplate.queryForObject(SQL_FIND_UNDER_OBSERVATION_BY_DUPLICATE_CHECK_ID, params, Boolean.class);

            if (underObservation == null) {
                throw new IllegalStateException("Column under_observation is null for customer with duplicateCheckId '%d'".formatted(duplicateCheckId));
            }

            log.debug(String.format("Customer '%d' under_observation=%s", duplicateCheckId, underObservation));
            return underObservation;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_DUPLICATE_CHECK_ID, duplicateCheckId));
            throw new IllegalStateException("No customer found for duplicateCheckId '" + duplicateCheckId + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to read under_observation flag for customer '%d'", duplicateCheckId), ex);
            throw new IllegalStateException("Could not read under_observation flag for duplicateCheckId '" + duplicateCheckId + "'", ex);
        }
    }

    @Override
    public boolean isUnderObservationByCustomerId(long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CUSTOMER_ID, customerId);

        try {
            Boolean underObservation = jdbcTemplate.queryForObject(SQL_FIND_UNDER_OBSERVATION_BY_CUSTOMER_ID, params, Boolean.class);

            if (underObservation == null) {
                throw new IllegalStateException("Column under_observation is null for customer with customerId '%d'".formatted(customerId));
            }

            log.debug(String.format("Customer '%d' under_observation=%s", customerId, underObservation));
            return underObservation;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID, customerId));
            throw new IllegalStateException("No customer found for customerId '" + customerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to read under_observation flag for customer '%d'", customerId), ex);
            throw new IllegalStateException("Could not read under_observation flag for customerId '" + customerId + "'", ex);
        }
    }
}
