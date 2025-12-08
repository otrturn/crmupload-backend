package com.crm.app.web;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.dto.RegisterResponse;
import com.crm.app.web.auth.AuthenticationService;
import com.crm.app.web.error.ReqgisterRequestInvalidDataException;
import com.crm.app.web.register.CustomerRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CustomerRegistrationService customerRegistrationService;

    public AuthController(AuthenticationService authenticationService,
                          CustomerRegistrationService customerRegistrationService) {
        this.authenticationService = authenticationService;
        this.customerRegistrationService = customerRegistrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-customer")
    public ResponseEntity<RegisterResponse> registerCustomer(@RequestBody RegisterRequest request) {
        return customerRegistrationService.registerCustomer(request);
    }

    @GetMapping("/test")
    public String test() {
        return "auth-ok";
    }

    @ExceptionHandler(ReqgisterRequestInvalidDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RegisterResponse handleRegisterRequestInvalidData(ReqgisterRequestInvalidDataException ex) {
        log.warn("registration: : {}", ex.getMessage());
        return new RegisterResponse("error: " + ex.getMessage());
    }

}