package com.crm.app.worker.process;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.worker.config.ConsumerUploadProperties;
import com.crm.app.worker.mail.UploadMailService;
import com.crm.app.worker.util.WorkerUtils;
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
import java.util.Optional;

import static com.crm.app.worker.util.WorkerUtils.writeExcelToFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadWorkerProcessForBexio {

    private final ConsumerUploadRepositoryPort repository;
    private final ConsumerUploadProperties properties;
    private final UploadHandlingForEspo uploadHandlingForEspo;

    private final BexioCtx bexioCtx;

    public void processUploadForEspo(ConsumerUploadContent upload) {
        Path excelSourceFile = Paths.get(String.format("%s/Upload_Bexio_%06d.xlsx", properties.getWorkdir(), upload.uploadId()));
        Path excelTargetFile = Paths.get(String.format("%s/Upload_Bexio_Korrektur_%06d.xlsx", properties.getWorkdir(), upload.uploadId()));
        log.info("Processing consumer_upload for Bexio uploadId={} sourceSysten={} crmSystem={}", upload.uploadId(), upload.sourceSystem(), upload.crmSystem());
        try {
            writeExcelToFile(upload.content(), excelSourceFile);

            List<ErrMsg> errors = new ArrayList<>();

            List<BexioEntry> bexioEntries = new ArrayList<>();
            new ReadBexioExcel().getEntries(excelSourceFile, bexioEntries, errors);
            log.info(String.format("Bexio %d entries read, %d errors", bexioEntries.size(), errors.size()));

            EspoEntityPool espoEntityPool = new EspoEntityPool();
            MyBexioToEspoMapper.toEspoAccounts(bexioCtx, bexioEntries, espoEntityPool, errors);
            log.info(String.format("Bexio %d entries mapped, %d errors", bexioEntries.size(), errors.size()));

            Optional<Consumer> consumer = repository.findConsumerByConsumerId(upload.consumerId());
            if (consumer.isPresent()) {
                uploadHandlingForEspo.processForEspo(upload, excelSourceFile, excelTargetFile, errors, consumer.get(), espoEntityPool);
            } else {
                log.error("Consumer not found for consumer id={}", upload.consumerId());
            }

        } catch (Exception ex) {
            repository.markUploadFailed(upload.uploadId(), ex.getMessage());
        }
        WorkerUtils.removeFile(excelSourceFile);
    }

}