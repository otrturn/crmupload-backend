package com.crm.app.adapter.jdbc.consumer;

import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Slf4j
@Repository
public class JdbcConsumerUploadRepositoryAdapter implements ConsumerUploadRepositoryPort {

    private static final String SEQUENCE_CONSUMER_UPLOAD_UPLOAD_ID = "app.sequence_consumer_upload";

    private static final String SQL_NEXT_UPLOAD_ID =
            "SELECT nextval('" + SEQUENCE_CONSUMER_UPLOAD_UPLOAD_ID + "')";

    private static final String SQL_FIND_CONSUMER_ID_BY_EMAIL =
            "SELECT c.consumer_id FROM app.consumer c WHERE c.email_address = :email_address";

    private static final String SQL_INSERT_CONSUMER_UPLOAD =
            "INSERT INTO app.consumer_upload " +
                    "(upload_id, consumer_id, source_system, crm_system, crm_customer_id, api_key, content, status) " +
                    "VALUES (:uploadId, :consumerId, :sourceSystem, :crmSystem, :crmCustomerId, :apiKey, :content, :status)";

    private static final String STATUS_NEW = "new";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcConsumerUploadRepositoryAdapter(final NamedParameterJdbcTemplate jdbcTemplate) {
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
                    "Sequence " + SEQUENCE_CONSUMER_UPLOAD_UPLOAD_ID + " returned null"
            );

            if (log.isDebugEnabled()) {
                log.debug("Generated next upload id: {}", nonNullNextId);
            }

            return nonNullNextId;
        } catch (DataAccessException ex) {
            log.error("Failed to obtain next upload id from sequence {}", SEQUENCE_CONSUMER_UPLOAD_UPLOAD_ID, ex);
            throw new IllegalStateException("Could not retrieve next upload id", ex);
        }
    }

    @Override
    public long findConsumerIdByEmail(final String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("email must not be null or blank");
        }

        final MapSqlParameterSource params = new MapSqlParameterSource("email_address", emailAddress);

        try {
            final Long consumerId = jdbcTemplate.queryForObject(
                    SQL_FIND_CONSUMER_ID_BY_EMAIL,
                    params,
                    Long.class
            );

            if (consumerId == null) {
                throw new IllegalStateException("Consumer ID for email '" + emailAddress + "' is null");
            }

            if (log.isDebugEnabled()) {
                log.debug("Found consumer id {} for email {}", consumerId, emailAddress);
            }

            return consumerId;
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No consumer found for email {}", emailAddress);
            throw new IllegalStateException("No consumer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to find consumer id for email {}", emailAddress, ex);
            throw new IllegalStateException("Could not retrieve consumer id for email '" + emailAddress + "'", ex);
        }
    }

    @Override
    public void insertConsumerUpload(
            final long uploadId,
            final long consumerId,
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
                .addValue("uploadId", uploadId)
                .addValue("consumerId", consumerId)
                .addValue("crmCustomerId", crmCustomerId)
                .addValue("sourceSystem", sourceSystem)
                .addValue("crmSystem", crmSystem)
                .addValue("apiKey", apiKey)
                .addValue("content", content)
                .addValue("status", STATUS_NEW);

        try {
            final int affectedRows = jdbcTemplate.update(SQL_INSERT_CONSUMER_UPLOAD, params);

            if (affectedRows != 1) {
                log.error("Insert into app.consumer_upload affected {} rows for uploadId={}", affectedRows, uploadId);
                throw new IllegalStateException(
                        "Insert into app.consumer_upload did not affect exactly one row (affected=" + affectedRows + ")"
                );
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Inserted consumer upload: uploadId={}, consumerId={}, crmCustomerId={}, status={}",
                        uploadId, consumerId, crmCustomerId, STATUS_NEW
                );
            }
        } catch (DataAccessException ex) {
            log.error(
                    "Failed to insert consumer upload for uploadId={}, consumerId={}, crmCustomerId={}",
                    uploadId, consumerId, crmCustomerId, ex
            );
            throw new IllegalStateException("Could not insert consumer upload", ex);
        }
    }


}
