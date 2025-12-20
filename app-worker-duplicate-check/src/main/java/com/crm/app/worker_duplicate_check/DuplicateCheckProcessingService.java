package com.crm.app.worker_duplicate_check;

import com.crm.app.dto.Customer;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.SourceSystem;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check.mail.DuplicatecheckMailService;
import com.crm.app.worker_duplicate_check.process.DuplicateCheckWorkerProcessForBexio;
import com.crm.app.worker_duplicate_check.process.DuplicateCheckWorkerProcessForLexware;
import com.crm.app.worker_duplicate_check.process.DuplicateCheckWorkerProcessForMyExcel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckProcessingService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final DuplicateCheckWorkerProcessForLexware duplicateCheckWorkerProcessForLexware;
    private final DuplicateCheckWorkerProcessForBexio duplicateCheckWorkerProcessForBexio;
    private final DuplicateCheckWorkerProcessForMyExcel duplicateCheckWorkerProcessForMyExcel;
    private final DuplicatecheckMailService duplicatecheckMailService;

    private static final String UNKNOWN_SOURCE_SYSTEM = "Unknown sourceSystem: ";

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    @Transactional
    public void processSingleDuplicateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        SourceSystem sourceSystem = SourceSystem.fromString(duplicateCheckContent.getSourceSystem());

        switch (sourceSystem) {
            case BEXIO ->
                    duplicateCheckWorkerProcessForBexio.processDuplicateCheckForVerification(duplicateCheckContent);
            case LEXWARE ->
                    duplicateCheckWorkerProcessForLexware.processDuplicateCheckForVerification(duplicateCheckContent);
            case MYEXCEL ->
                    duplicateCheckWorkerProcessForMyExcel.processDuplicateCheckForVerification(duplicateCheckContent);
            default -> {
                String msg = String.format("Unknown sourceSystem '%s' for duplicateCheckId=%d", duplicateCheckContent.getSourceSystem(), duplicateCheckContent.getDuplicateCheckId());
                log.warn(msg);
                duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), UNKNOWN_SOURCE_SYSTEM + duplicateCheckContent.getSourceSystem(), GSON.toJson(UNKNOWN_SOURCE_SYSTEM + duplicateCheckContent.getSourceSystem()));
            }
        }
    }

    @Transactional
    public void processSingleDuplicateCheckForFinalisation(DuplicateCheckContent duplicateCheckContent) {
        sendSuccessMailAndUpdateToDone(duplicateCheckContent);
    }

    private void sendSuccessMailAndUpdateToDone(DuplicateCheckContent duplicateCheckContent) {
        log.info(String.format("sendSuccessMailAndUpdateToDone for sourceSystem=%s duplicateCheckId=%d", duplicateCheckContent.getSourceSystem(), duplicateCheckContent.getDuplicateCheckId()));
        Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
        if (customer.isPresent()) {
            duplicatecheckMailService.sendSuccessMail(customer.get(), duplicateCheckContent, duplicateCheckContent.getContent());
            duplicateCheckRepositoryPort.markDuplicateCheckDone(duplicateCheckContent.getDuplicateCheckId());
        } else {
            log.error(String.format("Customer not found for customerId=%d", duplicateCheckContent.getCustomerId()));
        }
    }
}
