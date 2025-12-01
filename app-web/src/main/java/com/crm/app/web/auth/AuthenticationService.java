package com.crm.app.web.auth;

import com.crm.app.web.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authManager,
                                 UserDetailsService userDetailsService,
                                 JwtService jwtService) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        log.info("login request: {}", request);
        var authToken = new UsernamePasswordAuthenticationToken(
                request.username(), request.password()
        );

        try {
            authManager.authenticate(authToken);
            log.info("Login successful for username={}", request.username());
        } catch (AuthenticationException ex) {
            log.error("Authentication failed for username={}: {} - {}",
                    request.username(), ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token);
    }
}
