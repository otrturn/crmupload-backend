package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.CustomerUploadContent;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerUploadRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class JdbcCustomerUploadRepositoryAdapter implements CustomerUploadRepositoryPort {

    private static final String SEQUENCE_CUSTOMER_UPLOAD_UPLOAD_ID = "app.sequence_customer_upload";

    private static final String SQL_NEXT_UPLOAD_ID =
            "SELECT nextval('" + SEQUENCE_CUSTOMER_UPLOAD_UPLOAD_ID + "')";

    private static final String SQL_FIND_CUSTOMER_ID_BY_EMAIL =
            "SELECT c.customer_id FROM app.customer c WHERE c.email_address = :email_address";

    private static final String SQL_FIND_BY_CUSTOMER_ID = """
            SELECT customer_id,
                   user_id,
                   firstname,
                   lastname,
                   company_name,
                   email_address,
                   phone_number,
                   adrline1,
                   adrline2,
                   postalcode,
                   city,
                   country
              FROM app.customer
             WHERE customer_id = :customerId
            """;

    private static final String SQL_INSERT_CUSTOMER_UPLOAD =
            "INSERT INTO app.customer_upload " +
                    "(upload_id, customer_id, source_system, crm_system, crm_customer_id, api_key, content, status) " +
                    "VALUES (:uploadId, :customerId, :sourceSystem, :crmSystem, :crmCustomerId, :apiKey, :content, :status)";

    private static final String SQL_CLAIM_NEXT_UPLOADS = """
            UPDATE app.customer_upload cu
               SET status = 'processing'
             WHERE cu.upload_id IN (
                   SELECT upload_id
                     FROM app.customer_upload
                    WHERE status = 'new'
                    ORDER BY upload_id
                    FOR UPDATE SKIP LOCKED
                    LIMIT :limit
               )
             RETURNING upload_id
            """;

    private static final String SQL_MARK_DONE = """
            UPDATE app.customer_upload
               SET status = 'done',
                   last_error = NULL,
                   modified = now()
             WHERE upload_id = :uploadId
            """;

    private static final String SQL_MARK_FAILED = """
            UPDATE app.customer_upload
               SET status = 'failed',
                   last_error = :error,
                   modified = now()
             WHERE upload_id = :uploadId
            """;

    private static final String SQL_FIND_UPLOADS_BY_IDS = """
            SELECT upload_id,
                   customer_id,
                   source_system,
                   crm_system,
                   crm_customer_id,
                   api_key,
                   content
              FROM app.customer_upload
             WHERE upload_id = ANY(ARRAY[:uploadIds])
            """;

    private static final String STATUS_NEW = "new";
    private static final String LITERAL_UPLOADID = "uploadId";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcCustomerUploadRepositoryAdapter(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public long nextUploadId() {
        try {
            final Long nextId = jdbcTemplate.queryForObject(
                    SQL_NEXT_UPLOAD_ID,
                    new MapSqlParameterSource(),
                    Long.class
            );

            final Long nonNullNextId = Objects.requireNonNull(
                    nextId,
                    "Sequence " + SEQUENCE_CUSTOMER_UPLOAD_UPLOAD_ID + " returned null"
            );

            if (log.isDebugEnabled()) {
                log.debug("Generated next upload id: {}", nonNullNextId);
            }

            return nonNullNextId;
        } catch (DataAccessException ex) {
            log.error("Failed to obtain next upload id from sequence {}", SEQUENCE_CUSTOMER_UPLOAD_UPLOAD_ID, ex);
            throw new IllegalStateException("Could not retrieve next upload id", ex);
        }
    }

    @Override
    public long findCustomerIdByEmail(final String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("emailAddress must not be null or blank");
        }

        final MapSqlParameterSource params = new MapSqlParameterSource("email_address", emailAddress);

        try {
            final Long customerId = jdbcTemplate.queryForObject(
                    SQL_FIND_CUSTOMER_ID_BY_EMAIL,
                    params,
                    Long.class
            );

            if (customerId == null) {
                throw new IllegalStateException("Customer ID for emailAddress '" + emailAddress + "' is null");
            }

            if (log.isDebugEnabled()) {
                log.debug("Found customer id {} for emailAddress {}", customerId, emailAddress);
            }

            return customerId;
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No customer found for emailAddress {}", emailAddress);
            throw new IllegalStateException("No customer found for emailAddress '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to find customer id for emailAddress {}", emailAddress, ex);
            throw new IllegalStateException("Could not retrieve customer id for emailAddress '" + emailAddress + "'", ex);
        }
    }

    @Override
    public void insertCustomerUpload(
            final long uploadId,
            final long customerId,
            final String sourceSystem,
            final String crmSystem,
            final String crmCustomerId,
            final String apiKey,
            final byte[] content
    ) {
        if (crmCustomerId == null || crmCustomerId.isBlank()) {
            throw new IllegalArgumentException("crmCustomerId must not be null or blank");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be null or blank");
        }
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_UPLOADID, uploadId)
                .addValue("customerId", customerId)
                .addValue("crmCustomerId", crmCustomerId)
                .addValue("sourceSystem", sourceSystem)
                .addValue("crmSystem", crmSystem)
                .addValue("apiKey", apiKey)
                .addValue("content", content)
                .addValue("status", STATUS_NEW);

        try {
            final int affectedRows = jdbcTemplate.update(SQL_INSERT_CUSTOMER_UPLOAD, params);

            if (affectedRows != 1) {
                log.error("Insert into app.customer_upload affected {} rows for uploadId={}", affectedRows, uploadId);
                throw new IllegalStateException(
                        "Insert into app.customer_upload did not affect exactly one row (affected=" + affectedRows + ")"
                );
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Inserted customer upload: uploadId={}, customerId={}, crmCustomerId={}, status={}",
                        uploadId, customerId, crmCustomerId, STATUS_NEW
                );
            }
        } catch (DataAccessException ex) {
            log.error(
                    "Failed to insert customer upload for uploadId={}, customerId={}, crmCustomerId={}",
                    uploadId, customerId, crmCustomerId, ex
            );
            throw new IllegalStateException("Could not insert customer upload", ex);
        }
    }

    @Override
    public List<Long> claimNextUploads(final int limit) {
        final MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);
        try {
            return jdbcTemplate.query(
                    SQL_CLAIM_NEXT_UPLOADS,
                    params,
                    (rs, rowNum) -> rs.getLong("upload_id")
            );
        } catch (DataAccessException ex) {
            log.error("Failed to claim next uploads", ex);
            throw new IllegalStateException("Could not claim next uploads", ex);
        }
    }

    @Override
    public void markUploadDone(final long uploadId) {
        final MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_UPLOADID, uploadId);
        try {
            jdbcTemplate.update(SQL_MARK_DONE, params);
        } catch (DataAccessException ex) {
            log.error("Failed to mark upload {} as done", uploadId, ex);
            throw new IllegalStateException("Could not mark upload as done", ex);
        }
    }

    @Override
    public void markUploadFailed(final long uploadId, final String errorMessage) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_UPLOADID, uploadId)
                .addValue("error", errorMessage);
        try {
            jdbcTemplate.update(SQL_MARK_FAILED, params);
        } catch (DataAccessException ex) {
            log.error("Failed to mark upload {} as failed", uploadId, ex);
            throw new IllegalStateException("Could not mark upload as failed", ex);
        }
    }

    @Override
    public List<CustomerUploadContent> findUploadsByIds(List<Long> uploadIds) {
        if (uploadIds == null || uploadIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource("uploadIds", uploadIds);

        try {
            return jdbcTemplate.query(
                    SQL_FIND_UPLOADS_BY_IDS,
                    params,
                    (rs, rowNum) -> new CustomerUploadContent(
                            rs.getLong("upload_id"),
                            rs.getLong("customer_id"),
                            rs.getString("source_system"),
                            rs.getString("crm_system"),
                            rs.getString("crm_customer_id"),
                            rs.getString("api_key"),
                            rs.getBytes("content")
                    )
            );
        } catch (DataAccessException ex) {
            log.error("Failed to load customer_uploads for ids={}", uploadIds, ex);
            throw new IllegalStateException("Could not load customer uploads", ex);
        }
    }

    @Override
    public Optional<Customer> findCustomerByCustomerId(long customerId) {

        MapSqlParameterSource params =
                new MapSqlParameterSource("customerId", customerId);

        try {
            List<Customer> rows = jdbcTemplate.query(
                    SQL_FIND_BY_CUSTOMER_ID,
                    params,
                    (rs, rowNum) -> new Customer(
                            rs.getLong("customer_id"),
                            rs.getLong("user_id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("company_name"),
                            rs.getString("email_address"),
                            rs.getString("phone_number"),
                            rs.getString("adrline1"),
                            rs.getString("adrline2"),
                            rs.getString("postalcode"),
                            rs.getString("city"),
                            rs.getString("country")
                    )
            );

            if (rows.isEmpty()) {
                return Optional.empty();
            }

            if (rows.size() > 1) {
                throw new IllegalStateException(
                        "Mehrere Customers mit customer_id=" + customerId + " gefunden"
                );
            }

            return Optional.of(rows.get(0));

        } catch (DataAccessException ex) {
            log.error("Fehler beim Lesen von Customer customer_id={}", customerId, ex);
            throw new IllegalStateException(
                    "Fehler beim Lesen von Customer customer_id=" + customerId, ex
            );
        }
    }
}
