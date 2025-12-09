package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.*;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
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
public class JdbcCustomerRepositoryAdapter implements CustomerRepositoryPort {

    private static final String LITERAL_FIRSTNAME = "firstname";
    private static final String LITERAL_LASTNAME = "lastname";
    private static final String LITERAL_COUNTRY = "country";
    private static final String LITERAL_EMAIL_ADDRESS = "email_address";
    private static final String LITERAL_CUSTOMER_ID = "customer_id";
    private static final String LITERAL_CUSTOMER_ID_CAMCELCASE = "customerId";
    private static final String LITERAL_SOURCE_SYSTEM = "source_system";
    private static final String LITERAL_CRM_SYSTEM = "crm_system";
    private static final String LITERAL_CRM_URL = "crm_url";
    private static final String LITERAL_CRM_CUSTOMER_ID = "crm_customer_id";
    private static final String LITERAL_NO_CUSTOMER_FOR_EMAIL = "No customer found for email '{}'";

    private static final String SQL_FIND_ENABLED_BY_EMAIL =
            "SELECT enabled FROM app.customer WHERE email_address = :email_address";

    private static final String SQL_FIND_ENABLED_BY_CUSTOMER_ID =
            "SELECT enabled FROM app.customer WHERE customer_id = :customer_id";

    private static final String SQL_FIND_HAS_OPEN_CRM_UPLOADS_BY_EMAIL =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.crm_upload cu
                        JOIN app.customer c ON c.customer_id = cu.customer_id
                        WHERE c.email_address = :email_address
                          AND cu.status IN ('new', 'processing')
                    ) AS has_open_uploads;""";

    private static final String SQL_FIND_HAS_OPEN_UPLOADS_BY_CUSTOMER_ID =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.crm_upload cu
                        WHERE cu.customer_id = :customer_id
                          AND cu.status IN ('new', 'processing')
                    ) AS has_open_uploads;""";

    private static final String SQL_UPDATE_ENABLED = """
            UPDATE app.customer
               SET enabled = :enabled,
                   modified = now()
             WHERE customer_id = :customerId
            """;

    private static final String SQL_FIND_UPLOAD_HISTORY_BY_EMAIL = """
            SELECT
                cu.created         AS ts,
                cu.source_system   AS source_system,
                cu.crm_system      AS crm_system,
                cu.crm_url         AS crm_url,
                cu.crm_customer_id AS crm_customer_id,
                cu.status          AS status
            FROM app.crm_upload cu
            JOIN app.customer c
              ON c.customer_id = cu.customer_id
            WHERE c.email_address = :email_address
            ORDER BY cu.created DESC
            """;

    private static final String SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_CUSTOMER_ID = """
            SELECT source_system, crm_system, crm_customer_id
            FROM app.crm_upload
            WHERE customer_id = :customerId
              AND status = 'done'
            ORDER BY modified DESC
            LIMIT 1
            """;

    private static final String SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_EMAIL = """
            SELECT cu.source_system, cu.crm_system, cu.crm_url, cu.crm_customer_id
            FROM app.crm_upload cu
            JOIN app.customer c ON c.customer_id = cu.customer_id
            WHERE c.email_address = :email
              AND cu.status = 'done'
            ORDER BY cu.modified DESC
            LIMIT 1
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcCustomerRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean emailExists(String emailAddress) {
        String sql = """
                SELECT COUNT(*)
                FROM app.customer
                WHERE email_address = :email_address
                """;

        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress),
                Long.class);
        return count != null && count > 0;
    }

    @Override
    public long nextCustomerId() {
        String sql = "SELECT nextval('app.sequence_customer')";
        Long next = jdbc.getJdbcOperations().queryForObject(sql, Long.class);
        if (next == null) {
            throw new IllegalStateException("Could not obtain next customer_id");
        }
        return next;
    }

    @Override
    public void insertCustomer(Customer customer) {
        String sql = """
                INSERT INTO app.customer (
                    customer_id, user_id, firstname, lastname, company_name,
                    email_address, phone_number,
                    adrline1, adrline2, postalcode, city, country
                )
                VALUES (
                    :customerId, :userId, :firstname, :lastname, :companyName,
                    :email_address, :phone,
                    :adr1, :adr2, :postal, :city, :country
                )
                """;

        var params = new MapSqlParameterSource()
                .addValue(LITERAL_CUSTOMER_ID_CAMCELCASE, customer.customerId())
                .addValue("userId", customer.userId())
                .addValue(LITERAL_FIRSTNAME, customer.firstname())
                .addValue(LITERAL_LASTNAME, customer.lastname())
                .addValue("companyName", customer.companyName())
                .addValue(LITERAL_EMAIL_ADDRESS, customer.emailAddress())
                .addValue("phone", customer.phoneNumber())
                .addValue("adr1", customer.adrline1())
                .addValue("adr2", customer.adrline2())
                .addValue("postal", customer.postalcode())
                .addValue("city", customer.city())
                .addValue(LITERAL_COUNTRY, customer.country());

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
                        "Column enabled is null for customer with email '%s'".formatted(emailAddress)
                );
            }

            log.debug("Customer '{}' enabled={}", emailAddress, enabled);
            return enabled;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(LITERAL_NO_CUSTOMER_FOR_EMAIL, emailAddress);
            throw new IllegalStateException("No customer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read enabled flag for customer '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not read enabled flag for customer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isEnabledByCustomerId(long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CUSTOMER_ID, customerId);

        try {
            Boolean enabled = jdbc.queryForObject(
                    SQL_FIND_ENABLED_BY_CUSTOMER_ID,
                    params,
                    Boolean.class
            );

            if (enabled == null) {
                throw new IllegalStateException(
                        "Column enabled is null for customer with email '%d'".formatted(customerId)
                );
            }

            log.debug("Customer '{}' enabled={}", customerId, enabled);
            return enabled;
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No customer found for customerId '{}'", customerId);
            throw new IllegalStateException("No customer found for customerId '" + customerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read enabled flag for customer '{}'", customerId, ex);
            throw new IllegalStateException("Could not read enabled flag for customerId '" + customerId + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenCrmUploadsByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            Boolean hasOpenCrmUploads = jdbc.queryForObject(
                    SQL_FIND_HAS_OPEN_CRM_UPLOADS_BY_EMAIL,
                    params,
                    Boolean.class
            );

            if (hasOpenCrmUploads == null) {
                throw new IllegalStateException(
                        "Column hasOpenCrmUploads is null for customer with email '%s'".formatted(emailAddress)
                );
            }

            log.debug("Customer '{}' hasOpenCrmUploads={}", emailAddress, hasOpenCrmUploads);
            return hasOpenCrmUploads;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(LITERAL_NO_CUSTOMER_FOR_EMAIL, emailAddress);
            throw new IllegalStateException("No customer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read file pending for customer '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not read file pending for customer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenCrmUploadsByCustomerId(long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CUSTOMER_ID, customerId);

        try {
            Boolean hasOpenCrmUploads = jdbc.queryForObject(
                    SQL_FIND_HAS_OPEN_UPLOADS_BY_CUSTOMER_ID,
                    params,
                    Boolean.class
            );

            if (hasOpenCrmUploads == null) {
                throw new IllegalStateException(
                        "Column hasOpenCrmUploads is null for customer with customerId '%d'".formatted(customerId)
                );
            }

            log.debug("Customer '{}' hasOpenCrmUploads={}", customerId, hasOpenCrmUploads);
            return hasOpenCrmUploads;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(LITERAL_NO_CUSTOMER_FOR_EMAIL, customerId);
            throw new IllegalStateException("No customer found for customerId '" + customerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error("Failed to read file pending for customer '{}'", customerId, ex);
            throw new IllegalStateException("Could not read file pending for customer '" + customerId + "'", ex);
        }
    }

    @Override
    public void setEnabled(final long customerId, final boolean enabled) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_CUSTOMER_ID_CAMCELCASE, customerId)
                .addValue("enabled", enabled);

        try {
            int updated = jdbc.update(SQL_UPDATE_ENABLED, params);
            if (updated == 0) {
                throw new IllegalStateException("No customer found for customerId=" + customerId);
            }
        } catch (DataAccessException ex) {
            log.error("Failed to update enabled flag for customerId={}", customerId, ex);
            throw new IllegalStateException("Could not update enabled flag for customer " + customerId, ex);
        }
    }

    @Override
    public CustomerProfileResponse getCustomer(String emailAddress) {
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
                FROM app.customer
                WHERE email_address = :email
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", emailAddress);

        List<CustomerProfileResponse> result = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> mapToCustomerProfileResponse(rs, emailAddress)
        );

        return result.isEmpty() ? null : result.get(0);
    }

    private CustomerProfileResponse mapToCustomerProfileResponse(ResultSet rs, String emailAddress) throws SQLException {
        return new CustomerProfileResponse(
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
    public int updateCustomerProfile(String emailAddress, CustomerProfileRequest request) {
        String sql = """
                UPDATE app.customer
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
    public int updateCustomerPassword(String emailAddress, UpdatePasswordRequest request) {
        String sql = """
                UPDATE app.user_account ua
                   SET password = :password,
                       lastlogin = lastlogin -- unverändert lassen, nur damit es syntaktisch klar ist
                FROM app.customer c
                WHERE c.user_id = ua.id
                  AND c.email_address = :email_address
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("password", request.password())
                .addValue(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            int updated = jdbc.update(sql, params);
            if (updated == 0) {
                log.warn("No user_account/customer found for email '{}'", emailAddress);
            }
            return updated;
        } catch (DataAccessException ex) {
            log.error("Failed to update password for customer/user with email '{}'", emailAddress, ex);
            throw new IllegalStateException("Could not update password for email '" + emailAddress + "'", ex);
        }
    }

    @Override
    public List<CrmUploadHistory> findUploadHistoryByEmailAddress(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_EMAIL_ADDRESS, emailAddress);

        return jdbc.query(
                SQL_FIND_UPLOAD_HISTORY_BY_EMAIL,
                params,
                (rs, rowNum) -> new CrmUploadHistory(
                        rs.getTimestamp("ts"),
                        rs.getString(LITERAL_SOURCE_SYSTEM),
                        rs.getString(LITERAL_CRM_SYSTEM),
                        rs.getString(LITERAL_CRM_URL),
                        rs.getString(LITERAL_CRM_CUSTOMER_ID),
                        rs.getString("status")
                )
        );
    }

    public Optional<CrmUploadCoreInfo> findLatestUploadByCustomerId(long customerId) {
        Map<String, Object> params = Map.of(LITERAL_CUSTOMER_ID_CAMCELCASE, customerId);

        List<CrmUploadCoreInfo> list = jdbc.query(
                SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_CUSTOMER_ID,
                params,
                (rs, rowNum) -> new CrmUploadCoreInfo(
                        rs.getString(LITERAL_SOURCE_SYSTEM),
                        rs.getString(LITERAL_CRM_SYSTEM),
                        rs.getString(LITERAL_CRM_URL),
                        rs.getString(LITERAL_CRM_CUSTOMER_ID)
                )
        );

        return list.stream().findFirst();
    }

    public Optional<CrmUploadCoreInfo> findLatestUploadByEmail(String email) {
        Map<String, Object> params = Map.of("email", email);

        List<CrmUploadCoreInfo> list = jdbc.query(
                SQL_FIND_LATEST_SUCCESSFUL_UPLOAD_BY_EMAIL,
                params,
                (rs, rowNum) -> new CrmUploadCoreInfo(
                        rs.getString(LITERAL_SOURCE_SYSTEM),
                        rs.getString(LITERAL_CRM_SYSTEM),
                        rs.getString(LITERAL_CRM_URL),
                        rs.getString(LITERAL_CRM_CUSTOMER_ID)
                )
        );

        return list.stream().findFirst();
    }
}
