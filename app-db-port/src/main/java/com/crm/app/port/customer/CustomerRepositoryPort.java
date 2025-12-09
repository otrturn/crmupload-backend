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

    boolean isHasOpenCrmUploadsByEmail(String emailAddress);

    boolean isHasOpenCrmUploadsByCustomerId(long customerId);

    void setEnabled(long customerId, boolean enabled);

    CustomerProfileResponse getCustomer(String emailAddress);

    int updateCustomerProfile(String emailAddress, CustomerProfileRequest request);

    int updateCustomerPassword(String emailAddress, UpdatePasswordRequest request);

    List<CrmUploadHistory> findUploadHistoryByEmailAddress(String emailAddress);

    Optional<CrmInfo> findLatestUploadByCustomerId(long customerId);

    Optional<CrmInfo> findLatestUploadByEmail(String email);

}