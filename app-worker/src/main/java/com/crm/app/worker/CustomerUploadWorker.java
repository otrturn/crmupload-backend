package com.crm.app.worker;

import com.crm.app.dto.CrmSystem;
import com.crm.app.dto.CustomerUploadContent;
import com.crm.app.dto.SourceSystem;
import com.crm.app.port.customer.CustomerUploadRepositoryPort;
import com.crm.app.worker.config.CustomerUploadProperties;
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
public class CustomerUploadWorker {

    private final CustomerUploadRepositoryPort repository;
    private final CustomerUploadProperties properties;
    private final UploadWorkerProcessForBexio uploadWorkerProcessForBexio;
    private final UploadWorkerProcessForLexware uploadWorkerProcessForLexware;
    private final UploadWorkerProcessForMyExcel uploadWorkerProcessForMyExcel;

    @Scheduled(fixedDelayString = "${app.customer-upload.poll-interval-ms:10000}")
    @Transactional
    public void pollAndProcess() {
        final String UNKNOWN_CRM_SYSTEM = "Unknown crmSystem";
        final String UNKNOWN_SOURCE_SYSTEM = "Unknown sourceSystem";

        final List<Long> uploadIds = repository.claimNextUploads(properties.getBatchSize());

        if (uploadIds.isEmpty()) {
            return;
        }

        log.info("Claimed {} customer_upload job(s): {}", uploadIds.size(), uploadIds);

        List<CustomerUploadContent> uploads = repository.findUploadsByIds(uploadIds);

        for (CustomerUploadContent upload : uploads) {
            try {
                SourceSystem sourceSystem = SourceSystem.fromString(upload.sourceSystem());
                CrmSystem crmSystem = CrmSystem.fromString(upload.crmSystem());
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
                                repository.markUploadFailed(upload.uploadId(), UNKNOWN_CRM_SYSTEM + upload.crmSystem());
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
                                repository.markUploadFailed(upload.uploadId(), UNKNOWN_CRM_SYSTEM + upload.crmSystem());
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
                                repository.markUploadFailed(upload.uploadId(), UNKNOWN_CRM_SYSTEM + upload.crmSystem());
                            }
                        }
                        break;
                    }
                    default: {
                        repository.markUploadFailed(upload.uploadId(), UNKNOWN_SOURCE_SYSTEM + upload.sourceSystem());
                    }
                }
            } catch (Exception ex) {
                log.error("Error processing customer_upload with uploadId={}", upload.uploadId(), ex);
                repository.markUploadFailed(upload.uploadId(), ex.getMessage());
            }
        }
    }
}