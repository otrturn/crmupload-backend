package com.crm.app.web.duplicate_check;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.DuplicateCheckHistory;
import com.crm.app.dto.DuplicateCheckRequest;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.web.error.CustomerNotFoundException;
import com.crm.app.web.error.UploadNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DuplicateCheckService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;

    public void processDuplicateCheck(
            String emailAddress,
            String sourceSystem,
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("processUpload: duplicate-check file must not be empty");
        }

        log.info("Received upload: email={}", emailAddress);

        long customerId = customerRepositoryPort.findCustomerIdByEmail(emailAddress);
        log.info("Resolved customerId={} for email={}", customerId, emailAddress);

        List<String> products = customerRepositoryPort.findProductsByEmail(emailAddress);

        boolean enabled = customerRepositoryPort.isEnabledByCustomerId(customerId);
        boolean hasOpenDuplicatechecks = customerRepositoryPort.isHasOpenDuplicateChecksByCustomerId(customerId);

        if (!enabled) {
            throw new UploadNotAllowedException(String.format("processUpload: Customer %s is not enabled", emailAddress));
        }
        if (!products.contains(AppConstants.PRODUCT_DUPLICATE_CHECK)) {
            throw new UploadNotAllowedException(String.format("processUpload: Customer %s does not have product [%s]", emailAddress, AppConstants.PRODUCT_DUPLICATE_CHECK));
        }
        if (hasOpenDuplicatechecks) {
            throw new UploadNotAllowedException(String.format("processUpload: Customer %s has open duplicate-check", emailAddress));
        }

        long duplicateCheckId = duplicateCheckRepositoryPort.nextDuplicateCheckId();
        log.info("Generated duplicateCheckId={}", duplicateCheckId);

        try {
            duplicateCheckRepositoryPort.insertDuplicateCheck(new DuplicateCheckRequest(
                    duplicateCheckId,
                    customerId,
                    sourceSystem,
                    file.getBytes()
            ));
        } catch (Exception ex) {
            log.error("processUpload: Failed to insert duplicate-check: duplicateCheckId={}, customerId={}", duplicateCheckId, customerId, ex);
            throw new IllegalStateException("Upload failed: " + ex.getMessage(), ex);
        }
    }

    public List<DuplicateCheckHistory> getDuplicateCheckHistoryByEmail(String emailAddress) {
        List<DuplicateCheckHistory> response = customerRepositoryPort.findDuplicateCheckHistoryByEmailAddress(emailAddress);
        if (response == null) {
            throw new CustomerNotFoundException(emailAddress);
        }
        return response;
    }
}
