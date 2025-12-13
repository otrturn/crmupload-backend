package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.CrmUploadRequest;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
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
public class JdbcCrmUploadRepositoryAdapter implements CrmUploadRepositoryPort {

    private static final String SEQUENCE_CRM_UPLOAD_UPLOAD_ID = "app.sequence_crm_upload";

    private static final String SQL_NEXT_CRM_UPLOAD_ID =
            "SELECT nextval('" + SEQUENCE_CRM_UPLOAD_UPLOAD_ID + "')";

    private static final String SQL_INSERT_CRM_UPLOAD =
            "INSERT INTO app.crm_upload " +
                    "(upload_id, customer_id, source_system, crm_system, crm_url, crm_customer_id, api_key, content, status) " +
                    "VALUES (:uploadId, :customerId, :sourceSystem, :crmSystem, :crmUrl, :crmCustomerId, :apiKey, :content, :status)";

    private static final String SQL_CLAIM_NEXT_CRM_UPLOADS = """
            UPDATE app.crm_upload cu
               SET status = 'processing'
             WHERE cu.upload_id IN (
                   SELECT upload_id
                     FROM app.crm_upload
                    WHERE status = 'new'
                    ORDER BY upload_id
                    FOR UPDATE SKIP LOCKED
                    LIMIT :limit
               )
             RETURNING upload_id
            """;

    private static final String SQL_MARK_CRM_UPLOAD_DONE = """
            UPDATE app.crm_upload
               SET status = 'done',
                   content = NULL,
                   api_key = NULL,
                   last_error = NULL,
                   modified = now()
             WHERE upload_id = :uploadId
            """;

    private static final String SQL_MARK_CRM_UPLOAD_FAILED = """
            UPDATE app.crm_upload
               SET status = 'failed',
                   content = NULL,
                   api_key = NULL,
                   last_error = :error,
                   modified = now()
             WHERE upload_id = :uploadId
            """;

    private static final String SQL_FIND_CRM_UPLOADS_BY_IDS = """
            SELECT upload_id,
                   customer_id,
                   source_system,
                   crm_system,
                   crm_url,
                   crm_customer_id,
                   api_key,
                   content
              FROM app.crm_upload
             WHERE upload_id = ANY(ARRAY[:uploadIds])
            """;

    private static final String STATUS_CRM_UPLOAD_NEW = "new";

    // Parameter- und Spalten-LITERALS

    // Snake/lower-case
    private static final String LITERAL_UPLOAD_ID = "upload_id";
    private static final String LITERAL_CUSTOMER_ID = "customer_id";
    private static final String LITERAL_SOURCE_SYSTEM = "source_system";
    private static final String LITERAL_CRM_SYSTEM = "crm_system";
    private static final String LITERAL_CRM_URL = "crm_url";
    private static final String LITERAL_CRM_CUSTOMER_ID = "crm_customer_id";
    private static final String LITERAL_API_KEY = "api_key";
    private static final String LITERAL_CONTENT = "content";
    private static final String LITERAL_STATUS = "status";
    private static final String LITERAL_ERROR = "error";
    private static final String LITERAL_LIMIT = "limit";

    // camelCase-Parameter → *_CAMELCASE
    private static final String LITERAL_UPLOAD_ID_CAMELCASE = "uploadId";
    private static final String LITERAL_CUSTOMER_ID_CAMELCASE = "customerId";
    private static final String LITERAL_SOURCE_SYSTEM_CAMELCASE = "sourceSystem";
    private static final String LITERAL_CRM_SYSTEM_CAMELCASE = "crmSystem";
    private static final String LITERAL_CRM_URL_CAMELCASE = "crmUrl";
    private static final String LITERAL_CRM_CUSTOMER_ID_CAMELCASE = "crmCustomerId";
    private static final String LITERAL_API_KEY_CAMELCASE = "apiKey";
    private static final String LITERAL_UPLOAD_IDS_CAMELCASE = "uploadIds";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcCrmUploadRepositoryAdapter(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public long nextUploadId() {
        try {
            final Long nextId = jdbcTemplate.queryForObject(
                    SQL_NEXT_CRM_UPLOAD_ID,
                    new MapSqlParameterSource(),
                    Long.class
            );

            final Long nonNullNextId = Objects.requireNonNull(
                    nextId,
                    "Sequence " + SEQUENCE_CRM_UPLOAD_UPLOAD_ID + " returned null"
            );

            if (log.isDebugEnabled()) {
                log.debug("Generated next upload id: {}", nonNullNextId);
            }

            return nonNullNextId;
        } catch (DataAccessException ex) {
            log.error("Failed to obtain next upload id from sequence {}", SEQUENCE_CRM_UPLOAD_UPLOAD_ID, ex);
            throw new IllegalStateException("Could not retrieve next upload id", ex);
        }
    }

    @Override
    public void insertCrmUpload(CrmUploadRequest crmUploadRequest) {
        if (crmUploadRequest.getApiKey() == null || crmUploadRequest.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey must not be null or blank");
        }
        if (crmUploadRequest.getContent() == null) {
            throw new IllegalArgumentException("content must not be null");
        }

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_UPLOAD_ID_CAMELCASE, crmUploadRequest.getUploadId())
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, crmUploadRequest.getCustomerId())
                .addValue(LITERAL_CRM_CUSTOMER_ID_CAMELCASE, crmUploadRequest.getCrmCustomerId())
                .addValue(LITERAL_SOURCE_SYSTEM_CAMELCASE, crmUploadRequest.getSourceSystem())
                .addValue(LITERAL_CRM_SYSTEM_CAMELCASE, crmUploadRequest.getCrmSystem())
                .addValue(LITERAL_CRM_URL_CAMELCASE, crmUploadRequest.getCrmUrl())
                .addValue(LITERAL_API_KEY_CAMELCASE, crmUploadRequest.getApiKey())
                .addValue(LITERAL_CONTENT, crmUploadRequest.getContent())
                .addValue(LITERAL_STATUS, STATUS_CRM_UPLOAD_NEW);

        try {
            final int affectedRows = jdbcTemplate.update(SQL_INSERT_CRM_UPLOAD, params);

            if (affectedRows != 1) {
                log.error(String.format("Insert into app.crm_upload affected %d rows for uploadId=%d", affectedRows, crmUploadRequest.getUploadId()));
                throw new IllegalStateException(
                        "Insert into app.crm_upload did not affect exactly one row (affected=" + affectedRows + ")"
                );
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        String.format("Inserted customer upload: uploadId=%d, customerId=%d, crmCustomerId=%s, status=%s",
                        crmUploadRequest.getUploadId(),
                        crmUploadRequest.getCustomerId(),
                        crmUploadRequest.getCrmCustomerId(),
                        STATUS_CRM_UPLOAD_NEW)
                );
            }
        } catch (DataAccessException ex) {
            log.error(
                    String.format("Failed to insert customer upload for uploadId=%d, customerId=%d, crmCustomerId=%s",
                    crmUploadRequest.getUploadId(),
                    crmUploadRequest.getCustomerId(),
                    crmUploadRequest.getCrmCustomerId()),
                    ex
            );
            throw new IllegalStateException("Could not insert customer upload", ex);
        }
    }

    @Override
    public List<Long> claimNextUploads(final int limit) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_LIMIT, limit);
        try {
            return jdbcTemplate.query(
                    SQL_CLAIM_NEXT_CRM_UPLOADS,
                    params,
                    (rs, rowNum) -> rs.getLong(LITERAL_UPLOAD_ID)
            );
        } catch (DataAccessException ex) {
            log.error("Failed to claim next uploads", ex);
            throw new IllegalStateException("Could not claim next uploads", ex);
        }
    }

    @Override
    public void markUploadDone(final long uploadId) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_UPLOAD_ID_CAMELCASE, uploadId);
        try {
            jdbcTemplate.update(SQL_MARK_CRM_UPLOAD_DONE, params);
        } catch (DataAccessException ex) {
            log.error("Failed to mark upload {} as done", uploadId, ex);
            throw new IllegalStateException("Could not mark upload as done", ex);
        }
    }

    @Override
    public void markUploadFailed(final long uploadId, final String errorMessage) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_UPLOAD_ID_CAMELCASE, uploadId)
                .addValue(LITERAL_ERROR, errorMessage);
        try {
            jdbcTemplate.update(SQL_MARK_CRM_UPLOAD_FAILED, params);
        } catch (DataAccessException ex) {
            log.error("Failed to mark upload {} as failed", uploadId, ex);
            throw new IllegalStateException("Could not mark upload as failed", ex);
        }
    }

    @Override
    public List<CrmUploadContent> findUploadsByIds(List<Long> uploadIds) {
        if (uploadIds == null || uploadIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_UPLOAD_IDS_CAMELCASE, uploadIds);

        try {
            return jdbcTemplate.query(
                    SQL_FIND_CRM_UPLOADS_BY_IDS,
                    params,
                    (rs, rowNum) -> new CrmUploadContent(
                            rs.getLong(LITERAL_UPLOAD_ID),
                            rs.getLong(LITERAL_CUSTOMER_ID),
                            rs.getString(LITERAL_SOURCE_SYSTEM),
                            rs.getString(LITERAL_CRM_SYSTEM),
                            rs.getString(LITERAL_CRM_URL),
                            rs.getString(LITERAL_CRM_CUSTOMER_ID),
                            rs.getString(LITERAL_API_KEY),
                            rs.getBytes(LITERAL_CONTENT)
                    )
            );
        } catch (DataAccessException ex) {
            log.error("Failed to load crm_upload for ids={}", uploadIds, ex);
            throw new IllegalStateException("Could not load customer uploads", ex);
        }
    }

}
