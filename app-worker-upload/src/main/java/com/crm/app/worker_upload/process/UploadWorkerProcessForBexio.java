package com.crm.app.worker_upload.process;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.worker_upload.config.CrmUploadProperties;
import com.crmmacher.bexio_excel.dto.BexioColumn;
import com.crmmacher.bexio_excel.dto.BexioEntry;
import com.crmmacher.bexio_excel.reader.ReadBexioExcel;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoEntityPool;
import com.crmmacher.espo.importer.bexio_excel.config.BexioCtx;
import com.crmmacher.espo.importer.bexio_excel.util.MyBexioToEspoMapper;
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
public class UploadWorkerProcessForBexio {

    private final CrmUploadRepositoryPort repository;
    private final CrmUploadProperties properties;
    private final UploadHandlingForEspo uploadHandlingForEspo;
    private final CustomerRepositoryPort customerRepositoryPort;

    private final BexioCtx bexioCtx;

    public void processUploadForEspo(CrmUploadContent upload) {
        Path excelTargetFile = Paths.get(String.format("%s/Upload_Bexio_Korrektur_%06d.xlsx", properties.getWorkdir(), upload.getUploadId()));
        log.info(String.format("Processing crm_upload for Bexio uploadId=%d sourceSysten=%s crmSystem=%s", upload.getUploadId(), upload.getSourceSystem(), upload.getCrmSystem()));
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<BexioEntry> bexioEntries = new ArrayList<>();
            Map<BexioColumn, Integer> indexMap = new ReadBexioExcel().getEntries(upload.getContent(), bexioEntries, errors);
            log.info(String.format("Bexio %d entries read, %d errors", bexioEntries.size(), errors.size()));

            EspoEntityPool espoEntityPoolForReceived = new EspoEntityPool();
            MyBexioToEspoMapper.toEspoAccounts(bexioCtx, bexioEntries, espoEntityPoolForReceived, errors, indexMap);
            log.info(String.format("Bexio %d entries mapped, %d errors", bexioEntries.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(upload.getCustomerId());
            if (customer.isPresent()) {
                uploadHandlingForEspo.processForEspo(upload, upload.getContent(), excelTargetFile, errors, customer.get(), espoEntityPoolForReceived);
            } else {
                log.error(String.format("Customer not found for customerId=%d", upload.getCustomerId()));
            }
        } catch (Exception ex) {
            log.error("processUploadForEspo", ex);
        }
    }


}