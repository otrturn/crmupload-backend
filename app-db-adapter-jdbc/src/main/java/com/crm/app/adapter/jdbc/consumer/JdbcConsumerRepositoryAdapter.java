package com.crm.app.adapter.jdbc.consumer;

import com.crm.app.dto.*;
import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class JdbcConsumerRepositoryAdapter implements ConsumerRepositoryPort {

    private static final String LITERAL_FIRSTNAME = "firstname";
    private static final String LITERAL_LASTNAME = "lastname";
    private static final String LITERAL_COUNTRY = "country";
    private static final String LITERAL_EMAIL_ADDRESS = "email_address";
    private static final String LITERAL_CONSUMER_ID = "consumer_id";
    private static final String LITERAL_CONSUMER_ID_CAMCELCASE = "consumerId";
    private static final String LITERAL_SOURCE_SYSTEM = "source_system";
    private static final String LITERAL_CRM_SYSTEM = "crm_system";
    private static final String LITERAL_CRM_CUSTOMER_ID = "crm_customer_id";
    private static final String LITERAL_NO_CONSUMER_FOR_EMAIL = "No consumer found for email '{}'";

    private static final String SQL_FIND_ENABLED_BY_EMAIL =
            "SELECT enabled FROM app.consumer WHERE email_address = :email_address";

    private static final String SQL_FIND_ENABLED_BY_CONSUMER_ID =
            "SELECT enabled FROM app.consumer WHERE consumer_id = :consumer_id";

    private static final String SQL_FIND_HAS_OPEN_UPLOADS_BY_EMAIL =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.consumer_upload cu
                        JOIN app.consumer c ON c.consumer_id = cu.consumer_id
                        WHERE c.email_address = :email_address
                          AND cu.status IN ('new', 'processing')
                    ) AS has_open_uploads;""";

    private static final String SQL_FIND_HAS_OPEN_UPLOADS_BY_CONSUMER_ID =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.consumer_upload cu
                        WHERE cu.consumer_id = :consumer_id
                          AND cu.status IN ('new', 'processing')
                    ) AS has_open_uploads;""";

    private static final String SQL_UPDATE_ENABLED = """
            UPDATE app.consumer
               SET enabled = :enabled,
                   modified = now()
             WHERE consumer_id = :consumerId
            """;

    private static final String SQL_FIND_UPLOAD_HISTORY_BY_EMAIL = """
            SELECT
                cu.created         AS ts,
                cu.source_system   AS source_system,
                cu.crm_system      AS crm_system,
                cu.crm_customer_id AS crm_customer_id,
                cu.status          AS status
            FROM app.consumer_upload cu
            JOIN app.consumer c
              ON c.consumer_id = cu.consumer_id
            WHERE c.email_address = :email_address
            ORDER BY cu.created DESC
            """;

    private static final String SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_CONSUMER_ID = """
            SELECT source_system, crm_system, crm_customer_id
            FROM app.consumer_upload
            WHERE consumer_id = :consumerId
              AND status = 'done'
            ORDER BY modified DESC
            LIMIT 1
            """;

    private static final String SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_EMAIL = """
            SELECT cu.source_system, cu.crm_system, cu.crm_customer_id
            FROM app.consumer_upload cu
            JOIN app.consumer c ON c.consumer_id = cu.consumer_id
            WHERE c.email_address = :email
              AND cu.status = 'done'
            ORDER BY cu.modified DESC
            LIMIT 1
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcConsumerRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean emailExists(String emailAddress) {
        String sql = """
                SELECT COUNT(*)
                FROM app.consumer
                WHERE email_address = :email_address
                """;

        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress),
                Long.class);
        return count != null && count > 0;
    }

    @Override
    public long nextConsumerId() {
        String sql = "SELECT nextval('app.sequence_consumer')";
        Long next = jdbc.getJdbcOperations().queryForObject(sql, Long.class);
        if (next == null) {
            throw new IllegalStateException("Could not obtain next consumer_id");
        }
        return next;
    }

    @Override
    public void insertConsumer(Consumer consumer) {
        String sql = """
                INSERT INTO app.consumer (
                    consumer_id, user_id, firstname, lastname, company_name,
                    email_address, phone_number,
                    adrline1, adrline2, postalcode, city, country
                )
                VALUES (
                    :consumerId, :userId, :firstname, :lastname, :companyName,
                    :email_address, :phone,
                    :adr1, :adr2, :postal, :city, :country
                )
                """;

        var params = new MapSqlParameterSource()
                .addValue(LITERAL_CONSUMER_ID_CAMCELCASE, consumer.consumerId())
                .addValue("userId", consumer.userId())
                .addValue(LITERAL_FIRSTNAME, consumer.firstname())
                .addValue(LITERAL_LASTNAME, consumer.lastname())
                .addValue("companyName", consumer.companyName())
                .addValue(LITERAL_EMAIL_ADDRESS, consumer.emailAddress())
                .addValue("phone", consumer.phoneNumber())
                .addValue("adr1", consumer.adrline1())
                .addValue("adr2", consumer.adrline2())
                .addValue("postal", consumer.postalcode())
                .addValue("city", consumer.city())
                .addValue(LITERAL_COUNTRY, consumer.country());

        jdbc.update(sql, params);
    }

    @Override
    public boolean isEnabledByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            Boolean enabled = jdbc.queryForObject(
                    SQL_FIND_ENABLED_BY_EMAIL,
                    params,
                    Boolean.class
            );

            if (enabled == null) {
                throw new IllegalStateException(
                        "Column enabled is null for consumer with email '%s'".formatted(emailAddress)
                );
            }

            log.debug("Consumer '{}' enabled={}", emailAddress, enabled);
            return enabled;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(LITERAL_NO_CONSUMER_FOR_EMAIL, emailAddress);
            throw new IllegalStateException("No consumer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read enabled flag for consumer '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not read enabled flag for consumer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isEnabledByConsumerId(long consumerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CONSUMER_ID, consumerId);

        try {
            Boolean enabled = jdbc.queryForObject(
                    SQL_FIND_ENABLED_BY_CONSUMER_ID,
                    params,
                    Boolean.class
            );

            if (enabled == null) {
                throw new IllegalStateException(
                        "Column enabled is null for consumer with email '%d'".formatted(consumerId)
                );
            }

            log.debug("Consumer '{}' enabled={}", consumerId, enabled);
            return enabled;
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No consumer found for consumerId '{}'", consumerId);
            throw new IllegalStateException("No consumer found for consumerId '" + consumerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read enabled flag for consumer '{}'", consumerId, ex);
            throw new IllegalStateException("Could not read enabled flag for consumerId '" + consumerId + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenUploadsByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            Boolean hasOpenUploads = jdbc.queryForObject(
                    SQL_FIND_HAS_OPEN_UPLOADS_BY_EMAIL,
                    params,
                    Boolean.class
            );

            if (hasOpenUploads == null) {
                throw new IllegalStateException(
                        "Column hasOpenUploads is null for consumer with email '%s'".formatted(emailAddress)
                );
            }

            log.debug("Consumer '{}' hasOpenUploads={}", emailAddress, hasOpenUploads);
            return hasOpenUploads;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(LITERAL_NO_CONSUMER_FOR_EMAIL, emailAddress);
            throw new IllegalStateException("No consumer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read file pending for consumer '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not read file pending for consumer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenUploadsByConsumerId(long consumerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CONSUMER_ID, consumerId);

        try {
            Boolean hasOpenUploads = jdbc.queryForObject(
                    SQL_FIND_HAS_OPEN_UPLOADS_BY_CONSUMER_ID,
                    params,
                    Boolean.class
            );

            if (hasOpenUploads == null) {
                throw new IllegalStateException(
                        "Column hasOpenUploads is null for consumer with consumerId '%d'".formatted(consumerId)
                );
            }

            log.debug("Consumer '{}' hasOpenUploads={}", consumerId, hasOpenUploads);
            return hasOpenUploads;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(LITERAL_NO_CONSUMER_FOR_EMAIL, consumerId);
            throw new IllegalStateException("No consumer found for consumerId '" + consumerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read file pending for consumer '{}'", consumerId, ex);
            throw new IllegalStateException("Could not read file pending for consumer '" + consumerId + "'", ex);
        }
    }

    @Override
    public void setEnabled(final long consumerId, final boolean enabled) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_CONSUMER_ID_CAMCELCASE, consumerId)
                .addValue("enabled", enabled);

        try {
            int updated = jdbc.update(SQL_UPDATE_ENABLED, params);
            if (updated == 0) {
                throw new IllegalStateException("No consumer found for consumerId=" + consumerId);
            }
        } catch (DataAccessException ex) {
            log.error("Failed to update enabled flag for consumerId={}", consumerId, ex);
            throw new IllegalStateException("Could not update enabled flag for consumer " + consumerId, ex);
        }
    }

    @Override
    public ConsumerProfileResponse getConsumer(String emailAddress) {
        String sql = """
                SELECT firstname,
                       lastname,
                       company_name,
                       phone_number,
                       adrline1,
                       adrline2,
                       postalcode,
                       city,
                       country
                FROM app.consumer
                WHERE email_address = :email
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", emailAddress);

        List<ConsumerProfileResponse> result = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> mapToCustomerProfileResponse(rs, emailAddress)
        );

        return result.isEmpty() ? null : result.get(0);
    }

    private ConsumerProfileResponse mapToCustomerProfileResponse(ResultSet rs, String emailAddress) throws SQLException {
        return new ConsumerProfileResponse(
                rs.getString(LITERAL_FIRSTNAME),
                rs.getString(LITERAL_LASTNAME),
                rs.getString("company_name"),
                emailAddress,
                rs.getString("phone_number"),
                rs.getString("adrline1"),
                rs.getString("adrline2"),
                rs.getString("postalcode"),
                rs.getString("city"),
                rs.getString(LITERAL_COUNTRY)
        );
    }

    @Override
    public int updateConsumerProfile(String emailAddress, ConsumerProfileRequest request) {
        String sql = """
                UPDATE app.consumer
                SET firstname   = :firstname,
                    lastname    = :lastname,
                    company_name = :company_name,
                    phone_number = :phone_number,
                    adrline1    = :adrline1,
                    adrline2    = :adrline2,
                    postalcode  = :postalcode,
                    city        = :city,
                    country     = :country,
                    modified    = now()
                WHERE email_address = :email_address
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_FIRSTNAME, request.firstname())
                .addValue(LITERAL_LASTNAME, request.lastname())
                .addValue("company_name", request.company_name())
                .addValue("phone_number", request.phone_number())
                .addValue("adrline1", request.adrline1())
                .addValue("adrline2", request.adrline2())
                .addValue("postalcode", request.postalcode())
                .addValue("city", request.city())
                .addValue(LITERAL_COUNTRY, request.country())
                .addValue(LITERAL_EMAIL_ADDRESS, emailAddress);

        return jdbc.update(sql, params);
    }

    @Override
    public int updateConsumerPassword(String emailAddress, UpdatePasswordRequest request) {
        String sql = """
                UPDATE app.user_account ua
                   SET password = :password,
                       lastlogin = lastlogin -- unverändert lassen, nur damit es syntaktisch klar ist
                FROM app.consumer c
                WHERE c.user_id = ua.id
                  AND c.email_address = :email_address
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("password", request.password())
                .addValue(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            int updated = jdbc.update(sql, params);
            if (updated == 0) {
                log.warn("No user_account/consumer found for email '{}'", emailAddress);
            }
            return updated;
        } catch (DataAccessException ex) {
            log.error("Failed to update password for consumer/user with email '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not update password for email '" + emailAddress + "'", ex);
        }
    }

    @Override
    public List<ConsumerUploadHistory> findUploadHistoryByEmailAddress(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_EMAIL_ADDRESS, emailAddress);

        return jdbc.query(
                SQL_FIND_UPLOAD_HISTORY_BY_EMAIL,
                params,
                (rs, rowNum) -> new ConsumerUploadHistory(
                        rs.getTimestamp("ts"),
                        rs.getString(LITERAL_SOURCE_SYSTEM),
                        rs.getString(LITERAL_CRM_SYSTEM),
                        rs.getString(LITERAL_CRM_CUSTOMER_ID),
                        rs.getString("status")
                )
        );
    }

    public Optional<ConsumerUploadInfo> findLatestByConsumerId(long consumerId) {
        Map<String, Object> params = Map.of(LITERAL_CONSUMER_ID_CAMCELCASE, consumerId);

        List<ConsumerUploadInfo> list = jdbc.query(
                SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_CONSUMER_ID,
                params,
                (rs, rowNum) -> new ConsumerUploadInfo(
                        rs.getString(LITERAL_SOURCE_SYSTEM),
                        rs.getString(LITERAL_CRM_SYSTEM),
                        rs.getString(LITERAL_CRM_CUSTOMER_ID)
                )
        );

        return list.stream().findFirst();
    }

    public Optional<ConsumerUploadInfo> findLatestByEmail(String email) {
        Map<String, Object> params = Map.of("email", email);

        List<ConsumerUploadInfo> list = jdbc.query(
                SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_EMAIL,
                params,
                (rs, rowNum) -> new ConsumerUploadInfo(
                        rs.getString(LITERAL_SOURCE_SYSTEM),
                        rs.getString(LITERAL_CRM_SYSTEM),
                        rs.getString(LITERAL_CRM_CUSTOMER_ID)
                )
        );

        return list.stream().findFirst();
    }
}
