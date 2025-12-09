package com.crm.app.worker;

import com.crm.app.dto.CrmSystem;
import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.SourceSystem;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.worker.config.CrmUploadProperties;
import com.crm.app.worker.process.UploadWorkerProcessForBexio;
import com.crm.app.worker.process.UploadWorkerProcessForLexware;
import com.crm.app.worker.process.UploadWorkerProcessForMyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrmUploadWorker {

    private final CrmUploadRepositoryPort repository;
    private final CrmUploadProperties properties;
    private final UploadWorkerProcessForBexio uploadWorkerProcessForBexio;
    private final UploadWorkerProcessForLexware uploadWorkerProcessForLexware;
    private final UploadWorkerProcessForMyExcel uploadWorkerProcessForMyExcel;

    @Scheduled(fixedDelayString = "${app.crm-upload.poll-interval-ms:10000}")
    @Transactional
    public void pollAndProcess() {
        final String UNKNOWN_CRM_SYSTEM = "Unknown crmSystem";
        final String UNKNOWN_SOURCE_SYSTEM = "Unknown sourceSystem";

        final List<Long> uploadIds = repository.claimNextUploads(properties.getBatchSize());

        if (uploadIds.isEmpty()) {
            return;
        }

        log.info("Claimed {} crm_upload job(s): {}", uploadIds.size(), uploadIds);

        List<CrmUploadContent> uploads = repository.findUploadsByIds(uploadIds);

        for (CrmUploadContent upload : uploads) {
            try {
                SourceSystem sourceSystem = SourceSystem.fromString(upload.getSourceSystem());
                CrmSystem crmSystem = CrmSystem.fromString(upload.getCrmSystem());
                switch (sourceSystem) {
                    case BEXIO: {
                        switch (crmSystem) {
                            case ESPOCRM: {
                                uploadWorkerProcessForBexio.processUploadForEspo(upload);
                                break;
                            }
                            case PIPEDRIVE: {
                                //@Todo to be implemented
                                break;
                            }
                            default: {
                                repository.markUploadFailed(upload.getUploadId(), UNKNOWN_CRM_SYSTEM + upload.getCrmSystem());
                            }
                        }
                        break;
                    }
                    case LEXWARE: {
                        switch (crmSystem) {
                            case ESPOCRM: {
                                uploadWorkerProcessForLexware.processUploadForEspo(upload);
                                break;
                            }
                            case PIPEDRIVE: {
                                //@Todo to be implemented
                                break;
                            }
                            default: {
                                repository.markUploadFailed(upload.getUploadId(), UNKNOWN_CRM_SYSTEM + upload.getCrmSystem());
                            }
                        }
                        break;
                    }
                    case MYEXCEL: {
                        switch (crmSystem) {
                            case ESPOCRM: {
                                uploadWorkerProcessForMyExcel.processUploadForEspo(upload);
                                break;
                            }
                            case PIPEDRIVE: {
                                //@Todo to be implemented
                                break;
                            }
                            default: {
                                repository.markUploadFailed(upload.getUploadId(), UNKNOWN_CRM_SYSTEM + upload.getCrmSystem());
                            }
                        }
                        break;
                    }
                    default: {
                        repository.markUploadFailed(upload.getUploadId(), UNKNOWN_SOURCE_SYSTEM + upload.getSourceSystem());
                    }
                }
            } catch (Exception ex) {
                log.error("Error processing crm_upload with uploadId={}", upload.getUploadId(), ex);
                repository.markUploadFailed(upload.getUploadId(), ex.getMessage());
            }
        }
    }
}