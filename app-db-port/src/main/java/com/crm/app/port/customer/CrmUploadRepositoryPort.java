package com.crm.app.port.customer;

import com.crm.app.dto.CrmUploadContent;

import java.util.List;
import java.util.Optional;

public interface CrmUploadRepositoryPort {
    long nextUploadId();

    long findCustomerIdByEmail(String email);

    Optional<Customer> findCustomerByCustomerId(long customerId);

    void insertCrmUpload(
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

    List<CrmUploadContent> findUploadsByIds(List<Long> uploadIds);

}