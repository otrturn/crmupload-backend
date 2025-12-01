package com.crm.app.web.upload;

import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST-Controller zum Entgegennehmen und Persistieren von Consumer-Uploads.
 * Hexagonale Architektur: Dieser Controller spricht ausschließlich mit dem
 * ConsumerUploadRepositoryPort (Adapter implementiert: JdbcConsumerUploadRepositoryAdapter).
 */
@RestController
@RequestMapping("/api/consumer-upload")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ConsumerUploadController {

    private final ConsumerUploadRepositoryPort repository;

    /**
     * Empfängt einen Upload, ermittelt die Consumer-ID, erzeugt eine Upload-ID
     * und schreibt alles in die Tabelle app.consumer_upload.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(
            @RequestParam("emailAddress") @NotBlank String emailAddress,
            @RequestParam("crmCustomerId") @NotBlank String crmCustomerId,
            @RequestParam("crmApiKey") @NotBlank String crmApiKey,
            @RequestPart("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }

        log.info("Received upload: email={}, crmCustomerId={}", emailAddress, crmCustomerId);

        UploadRequest request = new UploadRequest(emailAddress, crmCustomerId, crmApiKey);

        // 1) Consumer-ID aus email bestimmen
        long consumerId = repository.findConsumerIdByEmail(request.emailAddress());
        log.debug("Resolved consumerId={} for email={}", consumerId, emailAddress);

        // 2) Upload-ID erzeugen
        long uploadId = repository.nextUploadId();
        log.debug("Generated uploadId={}", uploadId);

        // 3) Insert
        try {
            repository.insertConsumerUpload(
                    uploadId,
                    consumerId,
                    request.crmCustomerId(),
                    request.crmApiKey(),
                    file.getBytes()
            );
        } catch (Exception ex) {
            log.error("Failed to insert consumer upload: uploadId={}, consumerId={}", uploadId, consumerId, ex);
            throw new IllegalStateException("Upload failed: " + ex.getMessage(), ex);
        }

        return new UploadResponse("ok");
    }

    /**
     * Einfaches Fehlermapping für Requests, die unvollständig oder falsch sind.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public UploadResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new UploadResponse("error: " + ex.getMessage());
    }
}