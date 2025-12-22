package com.crm.app.web;

import com.crm.app.dto.UploadResponse;
import com.crm.app.web.upload.CrmUploadService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CrmUploadController {

    private final CrmUploadService uploadService;

    private static final String LITERAL_ERROR = "error: ";

    /**
     * Empfängt einen Upload, ermittelt die Customer-ID, erzeugt eine Upload-ID
     * und schreibt alles in die Tabelle app.crm_upload.
     */
    @PostMapping(path = "/crm-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("emailAddress") @NotBlank String emailAddress,
            @RequestParam("sourceSystem") @NotBlank String sourceSystem,
            @RequestParam("crmSystem") @NotBlank String crmSystem,
            @RequestParam(name = "crmUrl", required = false) String crmUrl,
            @RequestParam(name = "crmCustomerId", required = false) String crmCustomerId,
            @RequestParam("crmApiKey") @NotBlank String crmApiKey,
            @RequestPart("file") MultipartFile file
    ) {
        uploadService.processCrmUpload(emailAddress, sourceSystem, crmSystem, crmUrl, crmCustomerId, crmApiKey, file);
        return ResponseEntity.ok().build();
    }

}
