package com.crm.app.web.auth;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
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
    private final ConsumerRepositoryPort consumerRepositoryPort;

    public AuthenticationService(AuthenticationManager authManager,
                                 UserDetailsService userDetailsService,
                                 JwtService jwtService, ConsumerRepositoryPort consumerRepositoryPort) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.consumerRepositoryPort = consumerRepositoryPort;
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
        String emailAddress = request.username();
        String token = jwtService.generateToken(userDetails);

        boolean enabled = consumerRepositoryPort.isEnabledByEmail(emailAddress);
        boolean hasOpenUploads = consumerRepositoryPort.isHasOpenUploads(emailAddress);

        return new LoginResponse(token, enabled, hasOpenUploads);
    }
}
