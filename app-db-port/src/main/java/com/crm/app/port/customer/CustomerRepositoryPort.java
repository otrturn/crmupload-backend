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

    boolean isHasOpenDuplicateChecksByEmail(String emailAddress);

    boolean isHasOpenDuplicateChecksByCustomerId(long customerId);

    void setEnabled(long customerId, boolean enabled);

    CustomerProfileResponse getCustomer(String emailAddress);

    CustomerProfileResponse getCustomer(long customerId);

    int updateCustomerProfile(String emailAddress, CustomerProfileRequest request);

    int updateCustomerPassword(String emailAddress, UpdatePasswordRequest request);

    List<CrmUploadHistory> findUploadHistoryByEmailAddress(String emailAddress);

    List<DuplicateCheckHistory> findDuplicateCheckHistoryByEmailAddress(String emailAddress);

    Optional<CrmUploadCoreInfo> findLatestUploadByCustomerId(long customerId);

    Optional<CrmUploadCoreInfo> findLatestUploadByEmail(String email);

    List<String> findProductsByCustomerId(long customerId);

    List<String> findProductsByEmail(String email);

    long findCustomerIdByEmail(String email);

    Optional<Customer> findCustomerByCustomerId(long customerId);

}