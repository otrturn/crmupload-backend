package com.crm.app.web;

import com.crm.app.dto.UploadResponse;
import com.crm.app.web.duplicate_check.DuplicateCheckService;
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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DuplicateCheckController {

    private final DuplicateCheckService duplicateCheckService;

    private static final String LITERAL_ERROR = "error: ";

    @PostMapping(path = "/duplicate-check", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("emailAddress") @NotBlank String emailAddress,
            @RequestParam("sourceSystem") @NotBlank String sourceSystem,
            @RequestPart("file") MultipartFile file
    ) {
        duplicateCheckService.processDuplicateCheck(
                emailAddress,
                sourceSystem,
                file
        );

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(UploadNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public UploadResponse handleDuplicateCheckNotAllowed(UploadNotAllowedException ex) {
        log.warn("Duplicate-Check not allowed: {}", ex.getMessage());
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

    @ExceptionHandler(UploadAlreadyInProgressException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public UploadResponse handleDuplicateCheckAlreadyInProgress(UploadAlreadyInProgressException ex) {
        log.warn("Upload already in progress: {}", ex.getMessage());
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UploadResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

}