package com.crm.app.worker_upload.process;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.worker_upload.config.CrmUploadProperties;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoEntityPool;
import com.crmmacher.espo.importer.lexware_excel.config.LexwareCtx;
import com.crmmacher.espo.importer.lexware_excel.util.MyLexwareToEspoMapper;
import com.crmmacher.lexware_excel.dto.LexwareColumn;
import com.crmmacher.lexware_excel.dto.LexwareEntry;
import com.crmmacher.lexware_excel.reader.ReadLexwareExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadWorkerProcessForLexware {

    private final CrmUploadRepositoryPort repository;
    private final CrmUploadProperties properties;
    private final UploadHandlingForEspo uploadHandlingForEspo;
    private final CustomerRepositoryPort customerRepositoryPort;

    private final LexwareCtx lexwareCtx;

    public void processUploadForEspo(CrmUploadContent upload) {
        Path excelTargetFile = Paths.get(String.format("%s/Upload_Lexware_Korrektur_%06d.xlsx", properties.getWorkdir(), upload.getUploadId()));
        log.info(String.format("Processing crm_upload for Lexware uploadId=%d sourceSysten=%s crmSystem=%s", upload.getUploadId(), upload.getSourceSystem(), upload.getCrmSystem()));
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<LexwareEntry> lexwareEntries = new ArrayList<>();
            Map<LexwareColumn, Integer> indexMap = new ReadLexwareExcel().getEntries(upload.getContent(), lexwareEntries, errors);
            log.info(String.format("Lexware %d entries read, %d errors", lexwareEntries.size(), errors.size()));

            EspoEntityPool espoEntityPool = new EspoEntityPool();
            MyLexwareToEspoMapper.toEspoAccounts(lexwareCtx, lexwareEntries, espoEntityPool, errors, indexMap);
            log.info(String.format("Lexware %d entries mapped, %d errors", lexwareEntries.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(upload.getCustomerId());
            if (customer.isPresent()) {
                uploadHandlingForEspo.processForEspo(upload, upload.getContent(), excelTargetFile, errors, customer.get(), espoEntityPool);
            } else {
                log.error(String.format("Customer not found for customerId=%d", upload.getCustomerId()));
            }
        } catch (Exception ex) {
            log.error("processUploadForEspo", ex);
        }
    }
}