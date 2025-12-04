package com.crm.app.web.upload;

import com.crm.app.dto.UploadResponse;
import com.crm.app.web.error.UploadAlreadyInProgressException;
import com.crm.app.web.error.UploadNotAllowedException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST-Controller zum Entgegennehmen und Persistieren von Consumer-Uploads.
 * Hexagonale Architektur: Dieser Controller spricht ausschließlich mit dem
 * ConsumerUploadRepositoryPort (Adapter implementiert: JdbcConsumerUploadRepositoryAdapter).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ConsumerUploadController {

    private final ConsumerUploadService uploadService;

    private static final String LITERAL_ERROR = "error: ";

    /**
     * Empfängt einen Upload, ermittelt die Consumer-ID, erzeugt eine Upload-ID
     * und schreibt alles in die Tabelle app.consumer_upload.
     */
    @PostMapping(path = "/consumer-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("emailAddress") @NotBlank String emailAddress,
            @RequestParam("sourceSystem") @NotBlank String sourceSystem,
            @RequestParam("crmSystem") @NotBlank String crmSystem,
            @RequestParam("crmCustomerId") @NotBlank String crmCustomerId,
            @RequestParam("crmApiKey") @NotBlank String crmApiKey,
            @RequestPart("file") MultipartFile file
    ) {
        uploadService.processUpload(
                emailAddress,
                sourceSystem,
                crmSystem,
                crmCustomerId,
                crmApiKey,
                file
        );

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(UploadNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public UploadResponse handleUploadNotAllowed(UploadNotAllowedException ex) {
        log.warn("Upload not allowed: {}", ex.getMessage());
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

    @ExceptionHandler(UploadAlreadyInProgressException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public UploadResponse handleUploadAlreadyInProgress(UploadAlreadyInProgressException ex) {
        log.warn("Upload already in progress: {}", ex.getMessage());
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public UploadResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

}