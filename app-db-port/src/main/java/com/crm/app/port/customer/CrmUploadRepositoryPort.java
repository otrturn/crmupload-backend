package com.crm.app.port.customer;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.CrmUploadRequest;

import java.util.List;

public interface CrmUploadRepositoryPort {
    long nextUploadId();

    void insertCrmUpload(
            CrmUploadRequest crmUploadRequest
    );

    List<Long> claimNextUploads(int limit);

    void markUploadDone(long uploadId, String statisticsJson);

    void markUploadFailed(long uploadId, String errorMessage, String statisticsJson);

    List<CrmUploadContent> findUploadsByIds(List<Long> uploadIds);

    boolean isUnderObservationByUploadId(long uploadId);

    boolean isUnderObservationByCustomerId(long customerId);

}