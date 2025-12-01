package com.crm.app.web;

import com.crm.app.web.auth.LoginRequest;
import com.crm.app.web.auth.LoginResponse;
import com.crm.app.web.register.ConsumerRegistrationService;
import com.crm.app.web.register.RegisterRequest;
import com.crm.app.web.register.RegisterResponse;
import com.crm.app.web.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final ConsumerRegistrationService consumerRegistrationService;

    public AuthController(AuthenticationManager authManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          ConsumerRegistrationService consumerRegistrationService) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.consumerRegistrationService = consumerRegistrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(
                request.username(), request.password()
        );

        try {
            authManager.authenticate(authToken);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            log.error("Authentication failed: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(token));
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