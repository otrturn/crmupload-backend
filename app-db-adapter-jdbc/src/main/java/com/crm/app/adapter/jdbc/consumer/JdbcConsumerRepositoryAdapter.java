package com.crm.app.adapter.jdbc.consumer;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class JdbcConsumerRepositoryAdapter implements ConsumerRepositoryPort {

    private static final String LITERAL_EMAIL = "email_address";

    private static final String SQL_FIND_ENABLED_BY_EMAIL =
            "SELECT enabled FROM app.consumer WHERE email_address = :email_address";

    private static final String SQL_FIND_HAS_OPEN_UPLOADS =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.consumer_upload cu
                        JOIN app.consumer c ON c.consumer_id = cu.consumer_id
                        WHERE c.email_address = :email_address
                          AND cu.status IN ('new', 'processing')
                    ) AS has_open_uploads;""";

    private static final String SQL_UPDATE_ENABLED = """
            UPDATE app.consumer
               SET enabled = :enabled,
                   modified = now()
             WHERE consumer_id = :consumerId
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
                new MapSqlParameterSource(LITERAL_EMAIL, emailAddress),
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
                    consumer_id, user_id, firstname, lastname,
                    email_address, phone_number,
                    adrline1, adrline2, postalcode, city, country
                )
                VALUES (
                    :consumerId, :userId, :firstname, :lastname,
                    :email_address, :phone,
                    :adr1, :adr2, :postal, :city, :country
                )
                """;

        var params = new MapSqlParameterSource()
                .addValue("consumerId", consumer.consumerId())
                .addValue("userId", consumer.userId())
                .addValue("firstname", consumer.firstname())
                .addValue("lastname", consumer.lastname())
                .addValue(LITERAL_EMAIL, consumer.emailAddress())
                .addValue("phone", consumer.phoneNumber())
                .addValue("adr1", consumer.adrline1())
                .addValue("adr2", consumer.adrline2())
                .addValue("postal", consumer.postalcode())
                .addValue("city", consumer.city())
                .addValue("country", consumer.country());

        jdbc.update(sql, params);
    }

    @Override
    public boolean isEnabledByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL, emailAddress);

        try {
            Boolean enabled = jdbc.queryForObject(
                    SQL_FIND_ENABLED_BY_EMAIL,
                    params,
                    Boolean.class
            );

            if (enabled == null) {
                throw new IllegalStateException(
                        "Column enabled is null for consumer with email '%s'" .formatted(emailAddress)
                );
            }

            log.debug("Consumer '{}' enabled={}", emailAddress, enabled);
            return enabled;
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No consumer found for email '{}'", emailAddress);
            throw new IllegalStateException("No consumer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read enabled flag for consumer '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not read enabled flag for consumer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenUploads(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL, emailAddress);

        try {
            Boolean hasOpenUploads = jdbc.queryForObject(
                    SQL_FIND_HAS_OPEN_UPLOADS,
                    params,
                    Boolean.class
            );

            if (hasOpenUploads == null) {
                throw new IllegalStateException(
                        "Column enabled is null for consumer with email '%s'" .formatted(emailAddress)
                );
            }

            log.debug("Consumer '{}' hasOpenUploads={}", emailAddress, hasOpenUploads);
            return hasOpenUploads;
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No consumer found for email '{}'", emailAddress);
            throw new IllegalStateException("No consumer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read file pending for consumer '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not read file pending for consumer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public void setEnabled(final long consumerId, final boolean enabled) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("consumerId", consumerId)
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

}