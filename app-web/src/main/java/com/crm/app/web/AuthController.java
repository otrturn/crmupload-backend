package com.crm.app.web;

import com.crm.app.web.auth.AuthenticationService;
import com.crm.app.web.auth.LoginRequest;
import com.crm.app.web.auth.LoginResponse;
import com.crm.app.web.register.ConsumerRegistrationService;
import com.crm.app.web.register.RegisterRequest;
import com.crm.app.web.register.RegisterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ConsumerRegistrationService consumerRegistrationService;

    public AuthController(AuthenticationService authenticationService,
                          ConsumerRegistrationService consumerRegistrationService) {
        this.authenticationService = authenticationService;
        this.consumerRegistrationService = consumerRegistrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registerConsumer")
    public ResponseEntity<RegisterResponse> registerConsumer(@RequestBody RegisterRequest request) {
        return consumerRegistrationService.registerConsumer(request);
    }

    @GetMapping("/test")
    public String test() {
        return "auth-ok";
    }
}