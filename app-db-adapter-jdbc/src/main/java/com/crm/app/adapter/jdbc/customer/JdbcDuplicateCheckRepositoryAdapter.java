package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckRequest;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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

    private static final String SQL_DUPLICATE_CHECK_ID =
            "SELECT nextval('" + SEQUENCE_DUPLICATE_CHECK_ID + "')";

    private static final String SQL_INSERT_DUPLICATE_CHECK =
            "INSERT INTO app.duplicate_check " +
                    "(duplicate_check_id, customer_id, source_system, content, status) " +
                    "VALUES (:duplicateCheckId, :customerId, :sourceSystem, :content, :status)";

    private static final String SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_CHECK = """
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

    private static final String SQL_MARK_CHECKED = """
            UPDATE app.duplicate_check
               SET status = 'verified',
                   content = :content,
                   modified = now()
             WHERE duplicate_check_id = :duplicateCheckId
            """;

    private static final String SQL_MARK_FAILED = """
            UPDATE app.duplicate_check
               SET status = 'failed',
                   content = NULL,
                   last_error = :error,
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

    private static final String STATUS_DUPLICATE_CHECK_NEW = "new";

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
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, duplicateCheckRequest.getDuplicateCheckId())
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, duplicateCheckRequest.getCustomerId())
                .addValue(LITERAL_SOURCE_SYSTEM_CAMELCASE, duplicateCheckRequest.getSourceSystem())
                .addValue(LITERAL_CONTENT, duplicateCheckRequest.getContent())
                .addValue(LITERAL_STATUS, STATUS_DUPLICATE_CHECK_NEW);

        try {
            final int affectedRows = jdbcTemplate.update(SQL_INSERT_DUPLICATE_CHECK, params);

            if (affectedRows != 1) {
                log.error("Insert into app.crm_upload affected {} rows for uploadId={}", affectedRows, duplicateCheckRequest.getDuplicateCheckId());
                throw new IllegalStateException(
                        "Insert into app.crm_upload did not affect exactly one row (affected=" + affectedRows + ")"
                );
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Inserted duplicate-check: duplicateCheckId={}, customerId={}, status={}",
                        duplicateCheckRequest.getDuplicateCheckId(),
                        duplicateCheckRequest.getCustomerId(),
                        STATUS_DUPLICATE_CHECK_NEW);
            }
        } catch (DataAccessException ex) {
            log.error(
                    "Failed to insert duplicate-check upload for duplicateCheckId={}, customerId={}}",
                    duplicateCheckRequest.getDuplicateCheckId(),
                    duplicateCheckRequest.getCustomerId(),
                    ex
            );
            throw new IllegalStateException("Could not insert customer upload", ex);
        }
    }

    @Override
    public List<Long> claimNextDuplicateChecksForCheck(final int limit) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_LIMIT, limit);
        try {
            return jdbcTemplate.query(
                    SQL_CLAIM_NEXT_DUPLICATE_CHECK_IDS_FOR_CHECK,
                    params,
                    (rs, rowNum) -> rs.getLong(LITERAL_DUPLICATE_CHECK_ID)
            );
        } catch (DataAccessException ex) {
            log.error("Failed to claim duplicate checks for check", ex);
            throw new IllegalStateException("Could not claim next duplicate checks for check", ex);
        }
    }

    @Override
    public void markDuplicateCheckChecked(final long uploadId, byte[] content) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, uploadId)
                .addValue(LITERAL_CONTENT, content);
        try {
            jdbcTemplate.update(SQL_MARK_CHECKED, params);
        } catch (DataAccessException ex) {
            log.error("Failed to mark duplicate check {} as failed", uploadId, ex);
            throw new IllegalStateException("Could not mark duplicate check as checked", ex);
        }
    }

    @Override
    public void markDuplicateCheckFailed(final long uploadId, final String errorMessage) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_DUPLICATE_CHECK_ID_CAMELCASE, uploadId)
                .addValue(LITERAL_ERROR, errorMessage);
        try {
            jdbcTemplate.update(SQL_MARK_FAILED, params);
        } catch (DataAccessException ex) {
            log.error("Failed to mark duplicate check {} as failed", uploadId, ex);
            throw new IllegalStateException("Could not mark duplicate check as failed", ex);
        }
    }

    @Override
    public List<DuplicateCheckContent> findDuplicateChecksByIds(List<Long> duplicateCheckIds) {
        if (duplicateCheckIds == null || duplicateCheckIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_DUPLICATE_CHECK_IDS_CAMELCASE, duplicateCheckIds);

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
            log.error("Failed to load duplicate-check for ids={}", duplicateCheckIds, ex);
            throw new IllegalStateException("Could not load customer duplicate-check", ex);
        }
    }

}
