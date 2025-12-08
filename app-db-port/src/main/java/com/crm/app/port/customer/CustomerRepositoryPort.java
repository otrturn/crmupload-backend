package com.crm.app.port.customer;

import com.crm.app.dto.*;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {

    boolean emailExists(String emailAddress);

    long nextCustomerId();

    void insertCustomer(Customer customer);

    boolean isEnabledByEmail(String emailAddress);

    boolean isEnabledByCustomerId(long customerId);

    boolean isHasOpenUploadsByEmail(String emailAddress);

    boolean isHasOpenUploadsByCustomerId(long customerId);

    void setEnabled(long customerId, boolean enabled);

    CustomerProfileResponse getCustomer(String emailAddress);

    int updateCustomerProfile(String emailAddress, CustomerProfileRequest request);

    int updateCustomerPassword(String emailAddress, UpdatePasswordRequest request);

    List<CustomerUploadHistory> findUploadHistoryByEmailAddress(String emailAddress);

    Optional<CustomerUploadInfo> findLatestByCustomerId(long customerId);

    Optional<CustomerUploadInfo> findLatestByEmail(String email);

}