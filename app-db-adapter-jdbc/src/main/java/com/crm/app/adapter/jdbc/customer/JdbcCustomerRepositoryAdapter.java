package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.*;
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

    private static final String LITERAL_CUSTOMER_NUMBER = "customer_number";
    private static final String LITERAL_CUSTOMER_NUMBER_CAMELCASE = "customerNumber";
    private static final String LITERAL_FIRSTNAME = "firstname";
    private static final String LITERAL_LASTNAME = "lastname";
    private static final String LITERAL_COUNTRY = "country";
    private static final String LITERAL_EMAIL_ADDRESS = "email_address";
    private static final String LITERAL_EMAIL = "email";
    private static final String LITERAL_CUSTOMER_ID = "customer_id";
    private static final String LITERAL_CUSTOMER_ID_CAMELCASE = "customerId";
    private static final String LITERAL_USER_ID_CAMELCASE = "userId";
    private static final String LITERAL_COMPANY_NAME = "company_name";
    private static final String LITERAL_COMPANY_NAME_CAMELCASE = "companyName";
    private static final String LITERAL_PHONE_NUMBER = "phone_number";
    private static final String LITERAL_PHONE = "phone";
    private static final String LITERAL_TAX_ID = "tax_id";
    private static final String LITERAL_VAT_ID = "vat_id";
    private static final String LITERAL_TAX_ID_CAMELCASE = "taxId";
    private static final String LITERAL_VAT_ID_CAMELCASE = "vatId";
    private static final String LITERAL_ADRLINE1 = "adrline1";
    private static final String LITERAL_ADRLINE2 = "adrline2";
    private static final String LITERAL_POSTALCODE = "postalcode";
    private static final String LITERAL_ADR1 = "adr1";
    private static final String LITERAL_ADR2 = "adr2";
    private static final String LITERAL_POSTAL = "postal";
    private static final String LITERAL_CITY = "city";
    private static final String LITERAL_SOURCE_SYSTEM = "source_system";
    private static final String LITERAL_CRM_SYSTEM = "crm_system";
    private static final String LITERAL_CRM_URL = "crm_url";
    private static final String LITERAL_CRM_CUSTOMER_ID = "crm_customer_id";
    private static final String LITERAL_PRODUCT = "product";
    private static final String LITERAL_PASSWORD = "password";
    private static final String LITERAL_TS = "ts";
    private static final String LITERAL_STATUS = "status";
    private static final String LITERAL_USER_ID = "user_id";
    private static final String LITERAL_ACTIVATION_DATE = "activation_date";
    private static final String LITERAL_ENABLED = "enabled";
    private static final String LITERAL_IS_TEST = "is_test";

    private static final String LITERAL_NO_CUSTOMER_FOR_EMAIL = "No customer found for email '%s'";
    private static final String LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID = "No customer found for customerId '%s'";
    private static final String LITERAL_CUSTOMER_HAS_OPEN_CRM_UPLOADS = "Customer '%s' hasOpenCrmUploads=%s";
    private static final String LITERAL_CUSTOMER_HAS_OPEN_DUPLICATE_CHECKS = "Customer '%s' hasOpenDuplicateChecks=%s";
    private static final String LITERAL_FILE_READ_FAILED = "Failed to read file pending for customer '%s'";

    private static final String LITERAL_AGB_ACCEPTED_CAMELCASE = "agbAccepted";
    private static final String LITERAL_IS_ENTREPRENEUR_CAMELCASE = "isEntrepreneur";
    private static final String LITERAL_REQUEST_IMMEDIATE_SERVICE_START_CAMELCASE = "requestImmediateServiceStart";
    private static final String LITERAL_ACKNOWLEDGE_WITHDRAWAL_LOSS_CAMELCASE = "acknowledgeWithdrawalLoss";
    private static final String LITERAL_TERMS_VERSION_CAMELCASE = "termsVersion";
    private static final String LITERAL_IP_ADDRESS_CAMELCASE = "ipAddress";
    private static final String LITERAL_USER_AGENT_CAMELCASE = "userAgent";

    private static final String SQL_IS_CUSTOMER_BLOCKED = """
            SELECT EXISTS (
                SELECT 1
                FROM app.customer c
                JOIN app.customer_blocked cb
                  ON cb.customer_id = c.customer_id
                WHERE c.email_address = :email_address
            )
            """;

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
                          AND cu.status NOT IN ('done', 'failed')
                    ) AS has_open_uploads;""";

    private static final String SQL_FIND_HAS_OPEN_UPLOADS_BY_CUSTOMER_ID =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.crm_upload cu
                        WHERE cu.customer_id = :customer_id
                          AND cu.status NOT IN ('done', 'failed')
                    ) AS has_open_uploads;""";

    private static final String SQL_FIND_HAS_OPEN_DUPLICATE_CHECKS_BY_EMAIL =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.duplicate_check dc
                        JOIN app.customer c ON c.customer_id = dc.customer_id
                        WHERE c.email_address = :email_address
                          AND dc.status NOT IN ('done', 'failed')
                    ) AS has_open_duplicate_checks;""";

    private static final String SQL_FIND_HAS_OPEN_DUPLICATE_CHECKS_BY_CUSTOMER_ID =
            """
                    SELECT EXISTS (
                        SELECT 1
                        FROM app.duplicate_check dc
                        WHERE dc.customer_id = :customer_id
                          AND dc.status NOT IN ('done', 'failed')
                    ) AS has_open_duplicate_checks;""";

    private static final String SQL_UPDATE_CUSTOMER_TO_ENABLED = """
            UPDATE app.customer
               SET enabled = true,
                   activation_date = now(),
                   modified = now()
             WHERE customer_id = :customerId
            """;

    private static final String SQL_UPDATE_CUSTOMER_PRODUCTS_TO_ENABLED = """
            UPDATE app.customer_product
               SET enabled = true,
                   activation_date = now(),
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
                cu.status          AS status,
                cu.is_test         AS is_test
            FROM app.crm_upload cu
            JOIN app.customer c
              ON c.customer_id = cu.customer_id
            WHERE c.email_address = :email_address
            ORDER BY cu.created DESC
            """;

    private static final String SQL_FIND_LATEST_SUCCESSFUL_NO_TEST_UPLOAD_BY_CUSTOMER_ID = """
            SELECT source_system, crm_system, crm_url, crm_customer_id, is_test
            FROM app.crm_upload
            WHERE customer_id = :customerId
              AND status = 'done'
              AND is_test IS NOT TRUE
            ORDER BY modified DESC
            LIMIT 1
            """;

    private static final String SQL_FIND_LATEST_SUCCESSFUL_NO_TEST_UPLOAD_BY_EMAIL = """
            SELECT cu.source_system, cu.crm_system, cu.crm_url, cu.crm_customer_id, is_test
            FROM app.crm_upload cu
            JOIN app.customer c ON c.customer_id = cu.customer_id
            WHERE c.email_address = :email
              AND cu.status = 'done'
              AND is_test IS NOT TRUE
            ORDER BY cu.modified DESC
            LIMIT 1
            """;

    private static final String SQL_INSERT_CUSTOMER_PRODUCT = """
            INSERT INTO app.customer_product (customer_id, product)
            VALUES (:customerId, :product)
            ON CONFLICT DO NOTHING
            """;

    private static final String SQL_FIND_CUSTOMER_ID_BY_EMAIL =
            "SELECT c.customer_id FROM app.customer c WHERE c.email_address = :email_address";

    private static final String SQL_FIND_CUSTOMER_BY_CUSTOMER_ID = """
            SELECT customer_id,
                   customer_number,
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
                   country,
                   tax_id,
                   vat_id,
                   activation_date
              FROM app.customer
             WHERE customer_id = :customerId
            """;

    private static final String SQL_FIND_ACTIVE_PRODUCTS_BY_CUSTOMER_ID = """
            SELECT product
              FROM app.customer_product
             WHERE customer_id = :customerId
             AND   enabled = true
             ORDER BY product
            """;

    private static final String SQL_FIND_DUPLICATE_CHECK_HISTORY_BY_EMAIL = """
            SELECT
                dc.created         AS ts,
                dc.source_system   AS source_system,
                dc.status          AS status
            FROM app.duplicate_check dc
            JOIN app.customer c
              ON c.customer_id = dc.customer_id
            WHERE c.email_address = :email_address
            ORDER BY dc.created DESC
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

        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress), Long.class);
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
                    customer_id, customer_number, user_id, firstname, lastname, company_name,
                    email_address, phone_number,
                    adrline1, adrline2, postalcode, city, country,
                    tax_id, vat_id
                )
                VALUES (
                    :customerId, :customerNumber, :userId, :firstname, :lastname, :companyName,
                    :email_address, :phone,
                    :adr1, :adr2, :postal, :city, :country,
                    :taxId, :vatId
                )
                """;

        var params = new MapSqlParameterSource()
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customer.customerId())
                .addValue(LITERAL_CUSTOMER_NUMBER_CAMELCASE, customer.customerNumber())
                .addValue(LITERAL_USER_ID_CAMELCASE, customer.userId())
                .addValue(LITERAL_FIRSTNAME, customer.firstname())
                .addValue(LITERAL_LASTNAME, customer.lastname())
                .addValue(LITERAL_COMPANY_NAME_CAMELCASE, customer.companyName())
                .addValue(LITERAL_EMAIL_ADDRESS, customer.emailAddress())
                .addValue(LITERAL_PHONE, customer.phoneNumber())
                .addValue(LITERAL_ADR1, customer.adrline1())
                .addValue(LITERAL_ADR2, customer.adrline2())
                .addValue(LITERAL_POSTAL, customer.postalcode())
                .addValue(LITERAL_CITY, customer.city())
                .addValue(LITERAL_COUNTRY, customer.country())
                .addValue(LITERAL_TAX_ID_CAMELCASE, customer.taxId())
                .addValue(LITERAL_VAT_ID_CAMELCASE, customer.vatId());

        jdbc.update(sql, params);

        if (customer.products() != null && !customer.products().isEmpty()) {
            for (String product : customer.products()) {
                var productParams = new MapSqlParameterSource().addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customer.customerId()).addValue(LITERAL_PRODUCT, product);
                jdbc.update(SQL_INSERT_CUSTOMER_PRODUCT, productParams);
            }
        }
    }

    @Override
    public void insertCustomerAcknowledgement(CustomerAcknowledgement customerAcknowledgement) {
        String sql = """
                INSERT INTO app.customer_acknowledgement (
                    customer_id,
                    agb_accepted,
                    is_entrepreneur,
                    request_immediate_service_start,
                    acknowledge_withdrawal_loss,
                    terms_version,
                    ip_address,
                    userAgent
                )
                VALUES (
                    :customerId,
                    :agbAccepted,
                    :isEntrepreneur,
                    :requestImmediateServiceStart,
                    :acknowledgeWithdrawalLoss,
                    :termsVersion,
                    :ipAddress,
                    :userAgent
                )
                """;

        var params = new MapSqlParameterSource()
                .addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customerAcknowledgement.customerId())
                .addValue(LITERAL_AGB_ACCEPTED_CAMELCASE, customerAcknowledgement.isAgbAccepted())
                .addValue(LITERAL_IS_ENTREPRENEUR_CAMELCASE, customerAcknowledgement.isEntrepreneur())
                .addValue(LITERAL_REQUEST_IMMEDIATE_SERVICE_START_CAMELCASE, customerAcknowledgement.requestImmediateServiceStart())
                .addValue(LITERAL_ACKNOWLEDGE_WITHDRAWAL_LOSS_CAMELCASE, customerAcknowledgement.acknowledgeWithdrawalLoss())
                .addValue(LITERAL_TERMS_VERSION_CAMELCASE, customerAcknowledgement.termsVersion())
                .addValue(LITERAL_IP_ADDRESS_CAMELCASE, customerAcknowledgement.ipAddress())
                .addValue(LITERAL_USER_AGENT_CAMELCASE, customerAcknowledgement.userAgent());

        jdbc.update(sql, params);
    }

    @Override
    public boolean isBlockedByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            Boolean blocked = jdbc.queryForObject(SQL_IS_CUSTOMER_BLOCKED, params, Boolean.class);

            log.debug(String.format("Customer '%s' blocked=%s", emailAddress, blocked));
            return Boolean.TRUE.equals(blocked);
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_EMAIL, emailAddress));
            throw new IllegalStateException("No customer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to read blocked for customer '%s'", emailAddress), ex);
            throw new IllegalStateException("Could not read blocked for customer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isEnabledByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            Boolean enabled = jdbc.queryForObject(SQL_FIND_ENABLED_BY_EMAIL, params, Boolean.class);

            if (enabled == null) {
                throw new IllegalStateException("Column enabled is null for customer with email '%s'".formatted(emailAddress));
            }

            log.debug(String.format("Customer '%s' enabled=%s", emailAddress, enabled));
            return enabled;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_EMAIL, emailAddress));
            throw new IllegalStateException("No customer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to read enabled flag for customer '%s'", emailAddress), ex);
            throw new IllegalStateException("Could not read enabled flag for customer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isEnabledByCustomerId(long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CUSTOMER_ID, customerId);

        try {
            Boolean enabled = jdbc.queryForObject(SQL_FIND_ENABLED_BY_CUSTOMER_ID, params, Boolean.class);

            if (enabled == null) {
                throw new IllegalStateException("Column enabled is null for customer with customerId '%d'".formatted(customerId));
            }

            log.debug(String.format("Customer '%d' enabled=%s", customerId, enabled));
            return enabled;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID, customerId));
            throw new IllegalStateException("No customer found for customerId '" + customerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to read enabled flag for customer '%d'", customerId), ex);
            throw new IllegalStateException("Could not read enabled flag for customerId '" + customerId + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenCrmUploadsByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            Boolean hasOpenCrmUploads = jdbc.queryForObject(SQL_FIND_HAS_OPEN_CRM_UPLOADS_BY_EMAIL, params, Boolean.class);

            if (hasOpenCrmUploads == null) {
                throw new IllegalStateException("Column hasOpenCrmUploads is null for customer with email '%s'".formatted(emailAddress));
            }

            log.debug(String.format(LITERAL_CUSTOMER_HAS_OPEN_CRM_UPLOADS, emailAddress, hasOpenCrmUploads));
            return hasOpenCrmUploads;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_EMAIL, emailAddress));
            throw new IllegalStateException("No customer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format(LITERAL_FILE_READ_FAILED, emailAddress), ex);
            throw new IllegalStateException("Could not read file pending for customer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenCrmUploadsByCustomerId(long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CUSTOMER_ID, customerId);

        try {
            Boolean hasOpenCrmUploads = jdbc.queryForObject(SQL_FIND_HAS_OPEN_UPLOADS_BY_CUSTOMER_ID, params, Boolean.class);

            if (hasOpenCrmUploads == null) {
                throw new IllegalStateException("Column hasOpenCrmUploads is null for customer with customerId '%d'".formatted(customerId));
            }

            log.debug(String.format(LITERAL_CUSTOMER_HAS_OPEN_CRM_UPLOADS, customerId, hasOpenCrmUploads));
            return hasOpenCrmUploads;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID, customerId));
            throw new IllegalStateException("No customer found for customerId '" + customerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format(LITERAL_FILE_READ_FAILED, customerId), ex);
            throw new IllegalStateException("Could not read file pending for customer '" + customerId + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenDuplicateChecksByEmail(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            Boolean hasOpenDuplicateChecks = jdbc.queryForObject(SQL_FIND_HAS_OPEN_DUPLICATE_CHECKS_BY_EMAIL, params, Boolean.class);

            if (hasOpenDuplicateChecks == null) {
                throw new IllegalStateException("Column hasOpenDuplicateChecks is null for customer with email '%s'".formatted(emailAddress));
            }

            log.debug(String.format(LITERAL_CUSTOMER_HAS_OPEN_DUPLICATE_CHECKS, emailAddress, hasOpenDuplicateChecks));
            return hasOpenDuplicateChecks;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_EMAIL, emailAddress));
            throw new IllegalStateException("No customer found for email '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format(LITERAL_FILE_READ_FAILED, emailAddress), ex);
            throw new IllegalStateException("Could not read file pending for customer '" + emailAddress + "'", ex);
        }
    }

    @Override
    public boolean isHasOpenDuplicateChecksByCustomerId(long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource(LITERAL_CUSTOMER_ID, customerId);

        try {
            Boolean hasOpenDuplicateChecks = jdbc.queryForObject(SQL_FIND_HAS_OPEN_DUPLICATE_CHECKS_BY_CUSTOMER_ID, params, Boolean.class);

            if (hasOpenDuplicateChecks == null) {
                throw new IllegalStateException("Column hasOpenDuplicateChecks is null for customer with customerId '%d'".formatted(customerId));
            }

            log.debug(String.format(LITERAL_CUSTOMER_HAS_OPEN_DUPLICATE_CHECKS, customerId, hasOpenDuplicateChecks));
            return hasOpenDuplicateChecks;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format(LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID, customerId));
            throw new IllegalStateException("No customer found for customerId '" + customerId + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format(LITERAL_FILE_READ_FAILED, String.valueOf(customerId)), ex);
            throw new IllegalStateException("Could not read file pending for customer '" + customerId + "'", ex);
        }
    }

    @Override
    public void setCustomerToEnabled(final long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customerId);

        try {
            int updated = jdbc.update(SQL_UPDATE_CUSTOMER_TO_ENABLED, params);
            if (updated == 0) {
                throw new IllegalStateException("No customer found for customerId=" + customerId);
            }
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to update enabled flag for customerId=%d", customerId), ex);
            throw new IllegalStateException("Could not update enabled flag for customer " + customerId, ex);
        }
    }

    @Override
    public void setCustomerProductsToEnabled(final long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customerId);

        try {
            int updated = jdbc.update(SQL_UPDATE_CUSTOMER_PRODUCTS_TO_ENABLED, params);
            if (updated == 0) {
                throw new IllegalStateException("No customer found for customerId=" + customerId);
            }
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to update enabled flag for customerId=%d", customerId), ex);
            throw new IllegalStateException("Could not update enabled flag for customer products " + customerId, ex);
        }
    }

    @Override
    public CustomerProfile getCustomer(String emailAddress) {
        String sql = """
                SELECT customer_number,
                       firstname,
                       lastname,
                       company_name,
                       phone_number,
                       adrline1,
                       adrline2,
                       postalcode,
                       city,
                       country,
                       tax_id,
                       vat_id,
                       activation_date
                FROM app.customer
                WHERE email_address = :email
                """;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_EMAIL, emailAddress);

        List<CustomerProfile> result = jdbc.query(sql, params, (rs, rowNum) -> mapToCustomerProfileResponse(rs, emailAddress));

        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public CustomerProfile getCustomer(long customerId) {
        String sql = """
                SELECT customer_number,
                       firstname,
                       lastname,
                       company_name,
                       email_address,
                       phone_number,
                       adrline1,
                       adrline2,
                       postalcode,
                       city,
                       country,
                       tax_id,
                       vat_id,
                       activation_date
                FROM app.customer
                WHERE customer_id = :customer_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_CUSTOMER_ID, customerId);

        List<CustomerProfile> result = jdbc.query(sql, params, (rs, rowNum) -> mapToCustomerProfileResponse(rs));

        return result.isEmpty() ? null : result.get(0);
    }

    private CustomerProfile mapToCustomerProfileResponse(ResultSet rs, String emailAddress) throws SQLException {
        return new CustomerProfile(
                rs.getString(LITERAL_CUSTOMER_NUMBER),
                rs.getString(LITERAL_FIRSTNAME),
                rs.getString(LITERAL_LASTNAME),
                rs.getString(LITERAL_COMPANY_NAME),
                emailAddress,
                rs.getString(LITERAL_PHONE_NUMBER),
                rs.getString(LITERAL_ADRLINE1),
                rs.getString(LITERAL_ADRLINE2),
                rs.getString(LITERAL_POSTALCODE),
                rs.getString(LITERAL_CITY),
                rs.getString(LITERAL_COUNTRY),
                rs.getString(LITERAL_TAX_ID),
                rs.getString(LITERAL_VAT_ID),
                rs.getTimestamp(LITERAL_ACTIVATION_DATE)
        );
    }

    private CustomerProfile mapToCustomerProfileResponse(ResultSet rs) throws SQLException {
        return new CustomerProfile(
                rs.getString(LITERAL_CUSTOMER_NUMBER),
                rs.getString(LITERAL_FIRSTNAME),
                rs.getString(LITERAL_LASTNAME),
                rs.getString(LITERAL_COMPANY_NAME),
                rs.getString(LITERAL_EMAIL_ADDRESS),
                rs.getString(LITERAL_PHONE_NUMBER),
                rs.getString(LITERAL_ADRLINE1),
                rs.getString(LITERAL_ADRLINE2),
                rs.getString(LITERAL_POSTALCODE),
                rs.getString(LITERAL_CITY),
                rs.getString(LITERAL_COUNTRY),
                rs.getString(LITERAL_TAX_ID),
                rs.getString(LITERAL_VAT_ID),
                rs.getTimestamp(LITERAL_ACTIVATION_DATE)
        );
    }

    @Override
    public int updateCustomerProfile(String emailAddress, CustomerProfile request) {
        String sql = """
                UPDATE app.customer
                SET firstname    = :firstname,
                    lastname     = :lastname,
                    company_name = :company_name,
                    phone_number = :phone_number,
                    adrline1     = :adrline1,
                    adrline2     = :adrline2,
                    postalcode   = :postalcode,
                    city         = :city,
                    country      = :country,
                    tax_id       = :taxId,
                    vat_id       = :vatId,
                    modified     = now()
                WHERE email_address = :email_address
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_FIRSTNAME, request.firstname())
                .addValue(LITERAL_LASTNAME, request.lastname())
                .addValue(LITERAL_COMPANY_NAME, request.company_name())
                .addValue(LITERAL_PHONE_NUMBER, request.phone_number())
                .addValue(LITERAL_ADRLINE1, request.adrline1())
                .addValue(LITERAL_ADRLINE2, request.adrline2())
                .addValue(LITERAL_POSTALCODE, request.postalcode())
                .addValue(LITERAL_CITY, request.city())
                .addValue(LITERAL_COUNTRY, request.country())
                .addValue(LITERAL_TAX_ID_CAMELCASE, request.tax_id())
                .addValue(LITERAL_VAT_ID_CAMELCASE, request.vat_id())
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

        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_PASSWORD, request.password()).addValue(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            int updated = jdbc.update(sql, params);
            if (updated == 0) {
                log.warn(String.format("No user_account/customer found for email '%s'", emailAddress));
            }
            return updated;
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to update password for customer/user with email '%s'", emailAddress), ex);
            throw new IllegalStateException("Could not update password for email '" + emailAddress + "'", ex);
        }
    }

    @Override
    public List<CrmUploadHistory> findUploadHistoryByEmailAddress(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_EMAIL_ADDRESS, emailAddress);
        return jdbc.query(SQL_FIND_UPLOAD_HISTORY_BY_EMAIL, params, (rs, rowNum) -> new CrmUploadHistory(rs.getTimestamp(LITERAL_TS), rs.getString(LITERAL_SOURCE_SYSTEM), rs.getString(LITERAL_CRM_SYSTEM), rs.getString(LITERAL_CRM_URL), rs.getString(LITERAL_CRM_CUSTOMER_ID), rs.getString(LITERAL_STATUS), rs.getBoolean(LITERAL_IS_TEST)));
    }

    public Optional<CrmUploadCoreInfo> findLatestUploadByCustomerId(long customerId) {
        Map<String, Object> params = Map.of(LITERAL_CUSTOMER_ID_CAMELCASE, customerId);
        List<CrmUploadCoreInfo> list = jdbc.query(SQL_FIND_LATEST_SUCCESSFUL_NO_TEST_UPLOAD_BY_CUSTOMER_ID, params, (rs, rowNum) -> new CrmUploadCoreInfo(rs.getString(LITERAL_SOURCE_SYSTEM), rs.getString(LITERAL_CRM_SYSTEM), rs.getString(LITERAL_CRM_URL), rs.getString(LITERAL_CRM_CUSTOMER_ID), rs.getBoolean(LITERAL_IS_TEST)));
        return list.stream().findFirst();
    }

    public Optional<CrmUploadCoreInfo> findLatestSuccessfulNoTestUploadByEmail(String email) {
        Map<String, Object> params = Map.of(LITERAL_EMAIL, email);
        List<CrmUploadCoreInfo> list = jdbc.query(SQL_FIND_LATEST_SUCCESSFUL_NO_TEST_UPLOAD_BY_EMAIL, params, (rs, rowNum) -> new CrmUploadCoreInfo(rs.getString(LITERAL_SOURCE_SYSTEM), rs.getString(LITERAL_CRM_SYSTEM), rs.getString(LITERAL_CRM_URL), rs.getString(LITERAL_CRM_CUSTOMER_ID), rs.getBoolean(LITERAL_IS_TEST)));
        return list.stream().findFirst();
    }

    @Override
    public List<CustomerProduct> findActiveProductsByEmail(String email) {
        String sql = """
                SELECT cp.product,
                       cp.enabled,
                       cp.activation_date
                FROM app.customer c
                JOIN app.customer_product cp
                      ON cp.customer_id = c.customer_id
                WHERE c.email_address = :email
                ORDER BY cp.product
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(LITERAL_EMAIL, email);

        return jdbc.query(sql, params, (rs, rowNum) -> new CustomerProduct(
                rs.getString(LITERAL_PRODUCT),
                rs.getBoolean(LITERAL_ENABLED),
                rs.getTimestamp(LITERAL_ACTIVATION_DATE)
        ));
    }

    @Override
    public long findCustomerIdByEmail(final String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("emailAddress must not be null or blank");
        }

        final MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_EMAIL_ADDRESS, emailAddress);

        try {
            final Long customerId = jdbc.queryForObject(SQL_FIND_CUSTOMER_ID_BY_EMAIL, params, Long.class);

            if (customerId == null) {
                throw new IllegalStateException("Customer ID for emailAddress '" + emailAddress + "' is null");
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Found customer id %d for emailAddress %s", customerId, emailAddress));
            }

            return customerId;
        } catch (EmptyResultDataAccessException ex) {
            log.warn(String.format("No customer found for emailAddress %s", emailAddress));
            throw new IllegalStateException("No customer found for emailAddress '" + emailAddress + "'", ex);
        } catch (DataAccessException ex) {
            log.error(String.format("Failed to find customer id for emailAddress %s", emailAddress), ex);
            throw new IllegalStateException("Could not retrieve customer id for emailAddress '" + emailAddress + "'", ex);
        }
    }

    @Override
    public Optional<Customer> findCustomerByCustomerId(long customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customerId);

        try {
            List<Customer> rows = jdbc.query(SQL_FIND_CUSTOMER_BY_CUSTOMER_ID, params, (rs, rowNum) -> new Customer(rs.getLong(LITERAL_CUSTOMER_ID), rs.getString(LITERAL_CUSTOMER_NUMBER), rs.getLong(LITERAL_USER_ID), rs.getString(LITERAL_FIRSTNAME), rs.getString(LITERAL_LASTNAME), rs.getString(LITERAL_COMPANY_NAME), rs.getString(LITERAL_EMAIL_ADDRESS), rs.getString(LITERAL_PHONE_NUMBER), rs.getString(LITERAL_ADRLINE1), rs.getString(LITERAL_ADRLINE2), rs.getString(LITERAL_POSTALCODE), rs.getString(LITERAL_CITY), rs.getString(LITERAL_COUNTRY), rs.getString(LITERAL_TAX_ID), rs.getString(LITERAL_VAT_ID), null, rs.getTimestamp(LITERAL_ACTIVATION_DATE)));

            if (rows.isEmpty()) {
                return Optional.empty();
            }

            if (rows.size() > 1) {
                throw new IllegalStateException("Mehrere Customers mit customer_id=" + customerId + " gefunden");
            }

            Customer customer = rows.get(0);

            List<String> products = loadActiveProductsForCustomer(customerId);

            Customer enrichedCustomer = new Customer(customer.customerId(), customer.customerNumber(), customer.userId(), customer.firstname(), customer.lastname(), customer.companyName(), customer.emailAddress(), customer.phoneNumber(), customer.adrline1(), customer.adrline2(), customer.postalcode(), customer.city(), customer.country(), customer.taxId(), customer.vatId(), products, customer.activationDate());

            return Optional.of(enrichedCustomer);

        } catch (DataAccessException ex) {
            log.error(String.format("Fehler beim Lesen von Customer customer_id=%d", customerId), ex);
            throw new IllegalStateException("Fehler beim Lesen von Customer customer_id=" + customerId, ex);
        }
    }

    @Override
    public List<DuplicateCheckHistory> findDuplicateCheckHistoryByEmailAddress(String emailAddress) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_EMAIL_ADDRESS, emailAddress);
        return jdbc.query(SQL_FIND_DUPLICATE_CHECK_HISTORY_BY_EMAIL, params, (rs, rowNum) -> new DuplicateCheckHistory(rs.getTimestamp(LITERAL_TS), rs.getString(LITERAL_SOURCE_SYSTEM), rs.getString(LITERAL_STATUS)));
    }

    @Override
    public Optional<SimpleStats> getSimpleStats() {

        final String sql = """
                select
                    (select count(*) from app.customer) as customer,
                    (select count(*) from app.customer where enabled = true) as customer_enabled,
                    (select count(*) from app.customer_product where product = 'crm-upload') as product_crm_upload,
                    (select count(*) from app.customer_product where product = 'duplicate-check') as product_duplicate_check
                """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }

            SimpleStats stats = new SimpleStats();
            stats.setNCustomer(rs.getLong("customer"));
            stats.setNCustomerEnabled(rs.getLong("customer_enabled"));
            stats.setNProductsCrmUpload(rs.getLong("product_crm_upload"));
            stats.setNProductsDuplicateCheck(rs.getLong("product_duplicate_check"));

            return Optional.of(stats);
        });
    }

    private List<String> loadActiveProductsForCustomer(long customerId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue(LITERAL_CUSTOMER_ID_CAMELCASE, customerId);
            return jdbc.query(SQL_FIND_ACTIVE_PRODUCTS_BY_CUSTOMER_ID, params, (rs, rowNum) -> rs.getString(LITERAL_PRODUCT));
        } catch (DataAccessException ex) {
            log.error(String.format("Fehler beim Lesen der Produkte für customer_id=%d", customerId), ex);
            throw new IllegalStateException("Konnte Produkte für Customer nicht laden", ex);
        }
    }
}
