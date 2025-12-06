package com.crm.app.web;

import com.crm.app.dto.*;
import com.crm.app.web.consumer.ConsumerProfileService;
import com.crm.app.web.error.ConsumerNotFoundException;
import com.crm.app.web.upload.ConsumerUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerProfileService consumerProfileService;
    private final ConsumerUploadService consumerUploadService;

    @GetMapping("/me/{emailAddress:.+}")
    public ResponseEntity<ConsumerProfileResponse> getMe(@PathVariable("emailAddress") String emailAddress) {
        ConsumerProfileResponse response = consumerProfileService.getConsumerByEmail(emailAddress);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-consumer/{emailAddress:.+}")
    public ResponseEntity<Void> updateConsumer(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody ConsumerProfileRequest request
    ) {
        consumerProfileService.updateConsumerProfile(emailAddress, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-password/{emailAddress:.+}")
    public ResponseEntity<Void> updatePassword(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody UpdatePasswordRequest request
    ) {
        consumerProfileService.updateConsumerPassword(emailAddress, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-upload-history/{emailAddress:.+}")
    public ResponseEntity<ConsumerUploadHistoryResponse> getConsumerUploadHistory(@PathVariable("emailAddress") String emailAddress) {
        List<ConsumerUploadHistory> response = consumerUploadService.getConsumerUploadHistoryByEmail(emailAddress);
        return ResponseEntity.ok(new ConsumerUploadHistoryResponse(response));
    }

    @ExceptionHandler(ConsumerNotFoundException.class)
    public ResponseEntity<Void> handleConsumeNotFound(ConsumerNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
