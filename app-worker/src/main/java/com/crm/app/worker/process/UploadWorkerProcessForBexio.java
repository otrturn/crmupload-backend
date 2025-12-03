package com.crm.app.worker.process;

import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.worker.config.ConsumerUploadProperties;
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

import static com.crm.app.worker.util.WorkerUtils.writeExcelToFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadWorkerProcessForBexio {

    private final ConsumerUploadRepositoryPort repository;
    private final ConsumerUploadProperties properties;

    private final BexioCtx bexioCtx;

    public void processUpload(ConsumerUploadContent upload) {
        log.info("Processing consumer_upload for Bexio uploadId={} sourceSysten={} crmSystem={}", upload.uploadId(), upload.sourceSystem(), upload.crmSystem());
        try {
            Path excelFile = Paths.get(String.format("%s/Upload_Bexio_%06d.xlsx", properties.getWorkdir(), upload.uploadId()));
            writeExcelToFile(upload.content(), excelFile);
            List<BexioEntry> bexioEntries = new ArrayList<>();
            List<ErrMsg> errors = new ArrayList<>();
            EspoEntityPool espoEntityPool = new EspoEntityPool();
            new ReadBexioExcel().getEntries(excelFile, bexioEntries, errors);
            log.info(String.format("Bexio %d entries read, %d errors", bexioEntries.size(),errors.size()));
            MyBexioToEspoMapper.toEspoAccounts(bexioCtx, bexioEntries, espoEntityPool, errors);
            log.info(String.format("Bexio %d entries mapped, %d errors", bexioEntries.size(),errors.size()));
            repository.markUploadDone(upload.uploadId());
        } catch (Exception ex) {
            repository.markUploadFailed(upload.uploadId(), ex.getMessage());
        }
    }
}