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

    void markUploadDone(long uploadId);

    void markUploadFailed(long uploadId, String errorMessage);

    List<CrmUploadContent> findUploadsByIds(List<Long> uploadIds);

}