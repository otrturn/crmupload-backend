package com.crm.app.port.customer;

import com.crm.app.dto.*;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {

    boolean emailExists(String emailAddress);

    long nextCustomerId();

    void insertCustomer(Customer customer);

    void insertCustomerAcknowledgement(CustomerAcknowledgement customerAcknowledgement);

    boolean isEnabledByEmail(String emailAddress);

    boolean isEnabledByCustomerId(long customerId);

    boolean isHasOpenCrmUploadsByEmail(String emailAddress);

    boolean isHasOpenCrmUploadsByCustomerId(long customerId);

    boolean isHasOpenDuplicateChecksByEmail(String emailAddress);

    boolean isHasOpenDuplicateChecksByCustomerId(long customerId);

    void setCustomerToEnabled(long customerId);

    void setCustomerProductsToEnabled(long customerId);

    CustomerProfile getCustomer(String emailAddress);

    CustomerProfile getCustomer(long customerId);

    int updateCustomerProfile(String emailAddress, CustomerProfile request);

    int updateCustomerPassword(String emailAddress, UpdatePasswordRequest request);

    List<CrmUploadHistory> findUploadHistoryByEmailAddress(String emailAddress);

    List<DuplicateCheckHistory> findDuplicateCheckHistoryByEmailAddress(String emailAddress);

    Optional<CrmUploadCoreInfo> findLatestUploadByCustomerId(long customerId);

    Optional<CrmUploadCoreInfo> findLatestSuccessfulNoTestUploadByEmail(String email);

    List<CustomerProduct> findActiveProductsByEmail(String email);

    long findCustomerIdByEmail(String email);

    Optional<Customer> findCustomerByCustomerId(long customerId);

}