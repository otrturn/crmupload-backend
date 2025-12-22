package com.crm.app.web.upload;

import com.crm.app.dto.*;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.web.error.*;
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

    public void processCrmUpload(String emailAddress, String sourceSystem, String crmSystem, String crmUrl, String crmCustomerId, String crmApiKey, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("processCrmUpload: Uploaded file must not be empty");
        }

        log.info(String.format("Received processCrmUpload: email=%s, crmCustomerId=%s", emailAddress, crmCustomerId));

        long customerId = customerRepositoryPort.findCustomerIdByEmail(emailAddress);
        log.info(String.format("processCrmUpload resolved customerId=%d for email=%s", customerId, emailAddress));

        boolean enabled = customerRepositoryPort.isEnabledByCustomerId(customerId);
        boolean hasOpenCrmUploads = customerRepositoryPort.isHasOpenCrmUploadsByCustomerId(customerId);
        Optional<CrmUploadCoreInfo> crmUploadInfo = customerRepositoryPort.findLatestUploadByCustomerId(customerId);
        List<String> products = customerRepositoryPort.findActiveProductsByEmail(emailAddress);

        if (!enabled) {
            throw new UploadNotAllowedException(String.format("processCrmUpload: Customer %s is not enabled", emailAddress));
        }
        if (!SourceSystem.availableSourceSystems().contains(sourceSystem != null ? sourceSystem : "")) {
            throw new DuplicateCheckInvalidDataException(String.format("processCrmUpload: Customer %s unknown sourceSystem [%s]", emailAddress, sourceSystem));
        }
        if (!CrmSystem.availableCrmSystems().contains(crmSystem != null ? crmSystem : "")) {
            throw new DuplicateCheckInvalidDataException(String.format("processCrmUpload: Customer %s unknown crmSystem [%s]", emailAddress, crmSystem));
        }
        if (!products.contains(AppConstants.PRODUCT_CRM_UPLOAD)) {
            throw new UploadInvalidDataException(String.format("processCrmUpload: Customer %s does not have product [%s]", emailAddress, AppConstants.PRODUCT_CRM_UPLOAD));
        }
        if (hasOpenCrmUploads) {
            throw new UploadAlreadyInProgressException(String.format("processCrmUpload: Customer %s has open uploads", emailAddress));
        }
        if (crmUploadInfo.isPresent() && (!crmUploadInfo.get().getCrmSystem().equals(crmSystem != null ? crmSystem : "") || !crmUploadInfo.get().getCrmCustomerId().equals(crmCustomerId != null ? crmCustomerId : ""))) {
            throw new UploadInvalidDataException(String.format("processCrmUpload: crmSystem/crmCustomerId %s/%s [%s][%s] for customer %d invalid",
                    crmSystem,
                    crmCustomerId,
                    crmUploadInfo.get().getCrmSystem(),
                    crmUploadInfo.get().getCrmCustomerId(),
                    customerId));
        }

        long uploadId = repository.nextUploadId();
        log.info(String.format("Generated uploadId=%d", uploadId));

        try {
            repository.insertCrmUpload(new CrmUploadRequest(uploadId, customerId, sourceSystem, crmSystem, crmUrl, crmCustomerId, crmApiKey, file.getBytes()));
        } catch (Exception ex) {
            log.error(String.format("processCrmUpload: Failed to insert customer upload: uploadId=%d, customerId=%d", uploadId, customerId), ex);
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
