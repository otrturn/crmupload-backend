package com.crm.app.worker.process;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.worker.mail.UploadMailService;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoEntityPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadHandlingForEspo {

    private final ConsumerUploadRepositoryPort repository;
    private final UploadMailService uploadMailService;

    public void processForEspo(ConsumerUploadContent upload, List<ErrMsg> errors, Consumer consumer, EspoEntityPool espoEntityPool) {
        if (!ErrMsg.containsErrors(errors)) {
            repository.markUploadDone(upload.uploadId());
            uploadMailService.sendSuccessMailForEspo(consumer, upload, espoEntityPool);
        } else {
            repository.markUploadFailed(upload.uploadId(), "Validation failed");
            uploadMailService.sendErrorMailForEspo(consumer, upload, errors);
        }
    }
}
