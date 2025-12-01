package com.crm.app.web.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/consumer-upload")
@RequiredArgsConstructor
public class ConsumerUploadController {

    private final ConsumerUploadService consumerUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(
            @RequestParam("email") String email,
            @RequestParam("crmCustomerId") String crmCustomerId,
            @RequestParam("crmApiKey") String crmApiKey,
            @RequestPart("file") MultipartFile file
    ) {
        UploadRequest request = new UploadRequest(email, crmCustomerId, crmApiKey);
        consumerUploadService.handleUpload(request, file);
        return new UploadResponse("ok");
    }
}
