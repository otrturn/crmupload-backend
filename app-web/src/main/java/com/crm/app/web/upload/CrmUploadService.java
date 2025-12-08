package com.crm.app.web.upload;

import com.crm.app.dto.CrmUploadHistory;
import com.crm.app.dto.CrmUploadInfo;
import com.crm.app.dto.UploadRequest;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.web.error.CustomerNotFoundException;
import com.crm.app.web.error.UploadNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrmUploadService {

    private final CrmUploadRepositoryPort repository;
    private final CustomerRepositoryPort customerRepositoryPort;

    public void processUpload(
            String emailAddress,
            String sourceSystem,
            String crmSystem,
            String crmCustomerId,
            String crmApiKey,
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("processUpload: Uploaded file must not be empty");
        }

        log.info("Received upload: email={}, crmCustomerId={}", emailAddress, crmCustomerId);

        UploadRequest request = new UploadRequest(
                emailAddress,
                sourceSystem,
                crmSystem,
                crmCustomerId,
                crmApiKey
        );

        long customerId = repository.findCustomerIdByEmail(request.emailAddress());
        log.info("Resolved customerId={} for email={}", customerId, emailAddress);

        boolean enabled = customerRepositoryPort.isEnabledByCustomerId(customerId);
        boolean hasOpenCrmUploads = customerRepositoryPort.isHasOpenCrmUploadsByCustomerId(customerId);
        Optional<CrmUploadInfo> crmUploadInfo = customerRepositoryPort.findLatestByCustomerId(customerId);

        if (!enabled) {
            throw new UploadNotAllowedException(String.format("processUpload: Customer %s is not enabled", emailAddress));
        }
        if (hasOpenCrmUploads) {
            throw new UploadNotAllowedException(String.format("processUpload: Customer %s has open uploads", emailAddress));
        }
        if (crmUploadInfo.isPresent() && (!crmUploadInfo.get().crmSystem().equals(crmSystem)
                || !crmUploadInfo.get().crmCustomerId().equals(crmCustomerId))) {
            throw new UploadNotAllowedException(String.format("processUpload: crmSystem/crmCustomerId %s/%s for customer %d invalid",
                    crmSystem, crmCustomerId, customerId));
        }


        long uploadId = repository.nextUploadId();
        log.info("Generated uploadId={}", uploadId);

        try {
            repository.insertCrmUpload(
                    uploadId,
                    customerId,
                    sourceSystem,
                    crmSystem,
                    crmCustomerId,
                    crmApiKey,
                    file.getBytes()
            );
        } catch (Exception ex) {
            log.error("processUpload: Failed to insert customer upload: uploadId={}, customerId={}", uploadId, customerId, ex);
            throw new IllegalStateException("Upload failed: " + ex.getMessage(), ex);
        }
    }

    public List<CrmUploadHistory> getCrmUploadHistoryByEmail(String emailAddress) {
        List<CrmUploadHistory> response = customerRepositoryPort.findUploadHistoryByEmailAddress(emailAddress);
        if (response == null) {
            throw new CustomerNotFoundException(emailAddress);
        }
        return response;
    }

}
