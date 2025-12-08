package com.crm.app.port.customer;

import com.crm.app.dto.CustomerUploadContent;

import java.util.List;
import java.util.Optional;

public interface CustomerUploadRepositoryPort {
    long nextUploadId();

    long findCustomerIdByEmail(String email);

    Optional<Customer> findCustomerByCustomerId(long customerId);

    void insertCustomerUpload(
            long uploadId,
            long customerId,
            String sourceSystem,
            String crmSystem,
            String crmCustomerId,
            String apiKey,
            byte[] content
    );

    List<Long> claimNextUploads(int limit);

    void markUploadDone(long uploadId);

    void markUploadFailed(long uploadId, String errorMessage);

    List<CustomerUploadContent> findUploadsByIds(List<Long> uploadIds);

}