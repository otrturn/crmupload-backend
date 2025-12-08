package com.crm.app.worker.process;

import com.crm.app.dto.CustomerUploadContent;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerUploadRepositoryPort;
import com.crm.app.worker.config.CustomerUploadProperties;
import com.crm.app.worker.util.WorkerUtils;
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

import static com.crm.app.worker.util.WorkerUtils.writeExcelToFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadWorkerProcessForLexware {

    private final CustomerUploadRepositoryPort repository;
    private final CustomerUploadProperties properties;
    private final UploadHandlingForEspo uploadHandlingForEspo;

    private final LexwareCtx lexwareCtx;

    public void processUploadForEspo(CustomerUploadContent upload) {
        Path excelSourceFile = Paths.get(String.format("%s/Upload_Lexware_%06d.xlsx", properties.getWorkdir(), upload.uploadId()));
        Path excelTargetFile = Paths.get(String.format("%s/Upload_Lexware_Korrektur_%06d.xlsx", properties.getWorkdir(), upload.uploadId()));
        log.info("Processing customer_upload for Lexware uploadId={} sourceSysten={} crmSystem={}", upload.uploadId(), upload.sourceSystem(), upload.crmSystem());
        try {
            writeExcelToFile(upload.content(), excelSourceFile);
            List<ErrMsg> errors = new ArrayList<>();

            List<LexwareEntry> bexioEntries = new ArrayList<>();
            Map<LexwareColumn, Integer> indexMap = new ReadLexwareExcel().getEntries(excelSourceFile, bexioEntries, errors);
            log.info(String.format("Lexware %d entries read, %d errors", bexioEntries.size(), errors.size()));

            EspoEntityPool espoEntityPool = new EspoEntityPool();
            MyLexwareToEspoMapper.toEspoAccounts(lexwareCtx, bexioEntries, espoEntityPool, errors, indexMap);
            log.info(String.format("Lexware %d entries mapped, %d errors", bexioEntries.size(), errors.size()));

            Optional<Customer> customer = repository.findCustomerByCustomerId(upload.customerId());
            if (customer.isPresent()) {
                uploadHandlingForEspo.processForEspo(upload, excelSourceFile, excelTargetFile, errors, customer.get(), espoEntityPool);
            } else {
                log.error("Customer not found for customer id={}", upload.customerId());
            }
        } catch (Exception ex) {
            repository.markUploadFailed(upload.uploadId(), ex.getMessage());
        }
        WorkerUtils.removeFile(excelSourceFile);
    }
}