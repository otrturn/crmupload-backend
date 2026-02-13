package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.Customer;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_common.dto.StatisticsError;
import com.crm.app.worker_common.util.WorkerUtil;
import com.crm.app.worker_duplicate_check.mail.DuplicatecheckMailService;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.importer.lexware_excel.config.LexwareCtx;
import com.crmmacher.lexware_excel.dto.LexwareColumn;
import com.crmmacher.lexware_excel.dto.LexwareEntry;
import com.crmmacher.lexware_excel.reader.ReadLexwareExcel;
import com.crmmacher.util.ExcelUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckWorkerProcessForLexware {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final DuplicatecheckMailService duplicatecheckMailService;

    private final LexwareCtx lexwareCtx;

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public void processDuplicateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        log.info(String.format("Processing crm_upload for Lexware duplicateCheckId=%d sourceSysten=%s", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem()));
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<LexwareEntry> lexwareEntries = new ArrayList<>();
            Map<LexwareColumn, Integer> indexMap = new ReadLexwareExcel().getEntries(duplicateCheckContent.getContent(), lexwareEntries, errors);
            log.info(String.format("Lexware %d entries read, %d errors", lexwareEntries.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntries(lexwareEntries, indexMap, errors);
                log.info(String.format("processDuplicateCheck: %d entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
                if (!ErrMsg.containsErrors(errors)) {
                    duplicateCheckContent.setContent(WorkerUtil.createVerifiedExcelAsBytes(duplicateCheckEntries));
                    duplicateCheckRepositoryPort.markDuplicateCheckVerified(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
                } else {
                    duplicatecheckMailService.sendErrorMail(customer.get(), duplicateCheckContent, errors, ExcelUtils.markExcelFile(duplicateCheckContent.getContent(), errors));
                    StatisticsError statisticsError = new StatisticsError();
                    statisticsError.setFromErrMsg(errors);
                    duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), "Verification failed", GSON.toJson(statisticsError));
                }
            } else {
                log.error(String.format("Customer not found for customerId=%d", duplicateCheckContent.getCustomerId()));
            }
        } catch (Exception ex) {
            log.error(String.format("processDuplicateCheckForVerification: %s", ex.getMessage()), ex);
        }
    }

    private List<DuplicateCheckEntry> verifyAndMapEntries(List<LexwareEntry> lexwareEntries, Map<LexwareColumn, Integer> indexMap, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        boolean isSuccess = true;
        for (int i = 0; i < lexwareEntries.size(); i++) {
            LexwareEntry lexwareEntry = lexwareEntries.get(i);
            isSuccess = verifyEntry(indexMap, errors, lexwareEntry, i, isSuccess);
            if (isSuccess) {
                DuplicateCheckEntry duplicateCheckEntry = DuplicateCheckEntry.builder()
                        .cExternalReference(lexwareEntry.getcExternalReference())
                        .accountName(lexwareEntry.getAccountName())
                        .postalCode(lexwareEntry.getAddress().getPostcalCode())
                        .street(lexwareEntry.getAddress().getStreet())
                        .city(lexwareEntry.getAddress().getCity())
                        .country(lexwareEntry.getAddress().getCountry())
                        .emailAddress(
                                !lexwareEntry.getEmailAddressData().isEmpty()
                                        ? lexwareEntry.getEmailAddressData().get(0).getEmailAddress()
                                        : ""
                        )
                        .phoneNumber(
                                !lexwareEntry.getPhoneNumberData().isEmpty()
                                        ? lexwareEntry.getPhoneNumberData().get(0).getPhoneNumber()
                                        : ""
                        )
                        .build();

                duplicateCheckEntries.add(duplicateCheckEntry);
            }
        }
        return duplicateCheckEntries;
    }

    private static boolean verifyEntry(Map<LexwareColumn, Integer> indexMap, List<ErrMsg> errors, LexwareEntry lexwareEntry, int i, boolean isSuccess) {
        if (lexwareEntry.getAccountName() == null || lexwareEntry.getAccountName().isBlank()) {
            String msg = String.format("[Account] Zeile %d: Firmenname ist leer", i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.FIRMENNAME), LexwareColumn.FIRMENNAME.name(), msg));
            isSuccess = false;
        }
        if (lexwareEntry.getAddress().getPostcalCode() == null || lexwareEntry.getAddress().getPostcalCode().isBlank()) {
            String msg = String.format("[Account] Zeile %d: PLZ ist leer", i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.PLZ), LexwareColumn.PLZ.name(), msg));
            isSuccess = false;
        }
        if (lexwareEntry.getAddress().getStreet() == null || lexwareEntry.getAddress().getStreet().isBlank()) {
            String msg = String.format("[Account] Zeile %d: Strasse ist leer", i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.STRASSE), LexwareColumn.STRASSE.name(), msg));
            isSuccess = false;
        }
        if (lexwareEntry.getAddress().getCity() == null || lexwareEntry.getAddress().getCity().isBlank()) {
            String msg = String.format("[Account] Zeile %d: Land ist leer", i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.LAND), LexwareColumn.LAND.name(), msg));
            isSuccess = false;
        }
        return isSuccess;
    }
}
