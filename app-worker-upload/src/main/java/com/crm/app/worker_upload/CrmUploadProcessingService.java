package com.crm.app.worker_upload;

import com.crm.app.dto.CrmSystem;
import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.SourceSystem;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.worker_upload.process.UploadWorkerProcessForBexio;
import com.crm.app.worker_upload.process.UploadWorkerProcessForLexware;
import com.crm.app.worker_upload.process.UploadWorkerProcessForMyExcel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrmUploadProcessingService {

    private final CrmUploadRepositoryPort repository;
    private final UploadWorkerProcessForBexio uploadWorkerProcessForBexio;
    private final UploadWorkerProcessForLexware uploadWorkerProcessForLexware;
    private final UploadWorkerProcessForMyExcel uploadWorkerProcessForMyExcel;

    private static final String UNKNOWN_CRM_SYSTEM = "Unknown crmSystem: ";
    private static final String UNKNOWN_SOURCE_SYSTEM = "Unknown sourceSystem: ";
    private static final String ERROR_MSG = "Unknown crmSystem '%s' for uploadId=%d";

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    @Transactional
    public void processSingleUpload(CrmUploadContent upload) {
        SourceSystem sourceSystem = SourceSystem.fromString(upload.getSourceSystem());
        CrmSystem crmSystem = CrmSystem.fromString(upload.getCrmSystem());

        switch (sourceSystem) {
            case BEXIO -> handleBexio(upload, crmSystem);
            case LEXWARE -> handleLexware(upload, crmSystem);
            case MYEXCEL -> handleMyExcel(upload, crmSystem);
            default -> {
                log.error(String.format(ERROR_MSG, upload.getCrmSystem(), upload.getUploadId()));
                repository.markUploadFailed(upload.getUploadId(), UNKNOWN_SOURCE_SYSTEM + upload.getSourceSystem(), GSON.toJson(UNKNOWN_SOURCE_SYSTEM + upload.getSourceSystem()));
            }
        }
    }

    private void handleBexio(CrmUploadContent upload, CrmSystem crmSystem) {
        switch (crmSystem) {
            case ESPOCRM -> uploadWorkerProcessForBexio.processUploadForEspo(upload);
            case PIPEDRIVE -> {
                // TODO: implement
            }
            default -> {
                log.error(String.format(ERROR_MSG, upload.getCrmSystem(), upload.getUploadId()));
                repository.markUploadFailed(upload.getUploadId(), UNKNOWN_CRM_SYSTEM + upload.getCrmSystem(), GSON.toJson(UNKNOWN_SOURCE_SYSTEM + upload.getSourceSystem()));
            }
        }
    }

    private void handleLexware(CrmUploadContent upload, CrmSystem crmSystem) {
        switch (crmSystem) {
            case ESPOCRM -> uploadWorkerProcessForLexware.processUploadForEspo(upload);
            case PIPEDRIVE -> {
                // TODO: implement
            }
            default -> {
                log.error(String.format(ERROR_MSG, upload.getCrmSystem(), upload.getUploadId()));
                repository.markUploadFailed(upload.getUploadId(), UNKNOWN_CRM_SYSTEM + upload.getCrmSystem(), GSON.toJson(UNKNOWN_SOURCE_SYSTEM + upload.getSourceSystem()));
            }
        }
    }

    private void handleMyExcel(CrmUploadContent upload, CrmSystem crmSystem) {
        switch (crmSystem) {
            case ESPOCRM -> uploadWorkerProcessForMyExcel.processUploadForEspo(upload);
            case PIPEDRIVE -> {
                // TODO: implement
            }
            default -> {
                log.warn(String.format(ERROR_MSG, upload.getCrmSystem(), upload.getUploadId()));
                repository.markUploadFailed(upload.getUploadId(), UNKNOWN_CRM_SYSTEM + upload.getCrmSystem(), GSON.toJson(UNKNOWN_SOURCE_SYSTEM + upload.getSourceSystem()));
            }
        }
    }
}
