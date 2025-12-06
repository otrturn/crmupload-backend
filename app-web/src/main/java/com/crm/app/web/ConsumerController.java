package com.crm.app.web;

import com.crm.app.dto.ConsumerProfileRequest;
import com.crm.app.dto.ConsumerProfileResponse;
import com.crm.app.dto.UpdatePasswordRequest;
import com.crm.app.web.consumer.ConsumerProfileService;
import com.crm.app.web.error.ConsumerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerProfileService consumerProfileService;

    @GetMapping("/me/{emailAddress:.+}")
    public ResponseEntity<ConsumerProfileResponse> getMe(@PathVariable("emailAddress") String emailAddress) {
        ConsumerProfileResponse response = consumerProfileService.getCustomerByEmail(emailAddress);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateConsumer/{emailAddress:.+}")
    public ResponseEntity<Void> updateConsumer(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody ConsumerProfileRequest request
    ) {
        consumerProfileService.updateCustomerProfile(emailAddress, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/updatePassword/{emailAddress:.+}")
    public ResponseEntity<Void> updatePassword(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody UpdatePasswordRequest request
    ) {
        consumerProfileService.updateConsumerPassword(emailAddress, request);
        return ResponseEntity.noContent().build();
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
