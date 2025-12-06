package com.crm.app.web;

import com.crm.app.dto.ConsumerProfileRequest;
import com.crm.app.dto.ConsumerProfileResponse;
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

    @PutMapping("/update/{emailAddress:.+}")
    public ResponseEntity<Void> update(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody ConsumerProfileRequest request
    ) {
        consumerProfileService.updateCustomerProfile(emailAddress, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @ExceptionHandler(ConsumerNotFoundException.class)
    public ResponseEntity<Void> handleCustomerNotFound(ConsumerNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

}
