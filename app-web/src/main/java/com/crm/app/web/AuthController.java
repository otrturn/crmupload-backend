package com.crm.app.web;

import com.crm.app.web.auth.LoginRequest;
import com.crm.app.web.auth.LoginResponse;
import com.crm.app.web.security.JwtService;
import lombok.extern.slf4j.Slf4j;
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

    public AuthController(AuthenticationManager authManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(
                request.username(), request.password()
        );

        try {
            authManager.authenticate(authToken);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            log.error("RSX(ERR): Authentication failed: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token);
    }

    @GetMapping("/test")
    public String test() {
        return "auth-ok";
    }
}