package com.crm.app.web;

import com.crm.app.dto.UploadResponse;
import com.crm.app.web.config.AppWebDuplicatecheckProperties;
import com.crm.app.web.duplicate_check.DuplicateCheckService;
import com.crm.app.web.error.UploadAlreadyInProgressException;
import com.crm.app.web.error.UploadNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DuplicateCheckController {

    private final DuplicateCheckService duplicateCheckService;
    private final AppWebDuplicatecheckProperties appWebDuplicatecheckProperties;

    private static final String LITERAL_ERROR = "error: ";

    @PostMapping(path = "/duplicate-check", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("emailAddress") @NotBlank String emailAddress,
            @RequestParam("sourceSystem") @NotBlank String sourceSystem,
            @RequestPart("file") MultipartFile file
    ) {
        duplicateCheckService.processDuplicateCheck(emailAddress, sourceSystem, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/duplicate-check/downloads/help/excel-sample")
    public ResponseEntity<Resource> downloadExcelTemplate(HttpServletRequest request) throws IOException {
        log.info("Download(1) " + appWebDuplicatecheckProperties.getExcelSampleFile());
        Path path = Paths.get(appWebDuplicatecheckProperties.getExcelSampleFile());
        Resource resource = new UrlResource(path.toUri());
        log.info("Download(2) " + resource);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"excel-sample.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @ExceptionHandler(UploadNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public UploadResponse handleDuplicateCheckNotAllowed(UploadNotAllowedException ex) {
        log.warn(String.format("Duplicate-Check not allowed: %s", ex.getMessage()));
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

    @ExceptionHandler(UploadAlreadyInProgressException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public UploadResponse handleDuplicateCheckAlreadyInProgress(UploadAlreadyInProgressException ex) {
        log.warn(String.format("Upload already in progress: %s", ex.getMessage()));
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UploadResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn(String.format("Bad request: %s", ex.getMessage()));
        return new UploadResponse(LITERAL_ERROR + ex.getMessage());
    }
}
