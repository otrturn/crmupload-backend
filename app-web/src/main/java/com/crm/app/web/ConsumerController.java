package com.crm.app.web;

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

    @GetMapping("/me/{email:.+}")
    public ResponseEntity<ConsumerProfileResponse> getMe(@PathVariable("email") String email) {
        ConsumerProfileResponse response = consumerProfileService.getCustomerByEmail(email);
        return ResponseEntity.ok(response);
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
