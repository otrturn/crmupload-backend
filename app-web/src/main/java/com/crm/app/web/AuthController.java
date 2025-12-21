package com.crm.app.web;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.dto.RegisterResponse;
import com.crm.app.web.auth.AuthenticationService;
import com.crm.app.web.register.CustomerRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.crm.app.web.util.WebUtils.extractClientIp;

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
    public ResponseEntity<RegisterResponse> registerCustomer(@RequestBody RegisterRequest request,
                                                             HttpServletRequest httpRequest) {
        String ipAddress = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return customerRegistrationService.registerCustomer(request, ipAddress, userAgent);
    }

    @GetMapping("/test")
    public String test() {
        return "auth-ok";
    }

}
