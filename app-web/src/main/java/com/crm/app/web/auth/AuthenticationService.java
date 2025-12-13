package com.crm.app.web.auth;

import com.crm.app.dto.CrmUploadCoreInfo;
import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.web.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final CustomerRepositoryPort customerRepositoryPort;

    public AuthenticationService(AuthenticationManager authManager,
                                 UserDetailsService userDetailsService,
                                 JwtService jwtService, CustomerRepositoryPort customerRepositoryPort) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.customerRepositoryPort = customerRepositoryPort;
    }

    public LoginResponse login(LoginRequest request) {
        log.info(String.format("login request: %s", String.valueOf(request)));
        var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());

        try {
            authManager.authenticate(authToken);
            log.info(String.format("Login successful for username=%s", request.username()));
        } catch (AuthenticationException ex) {
            log.error(String.format("Authentication failed for username=%s: %s - %s", request.username(), ex.getClass().getSimpleName(), ex.getMessage()), ex);
            throw ex;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String emailAddress = request.username();
        String token = jwtService.generateToken(userDetails);

        boolean enabled = customerRepositoryPort.isEnabledByEmail(emailAddress);
        boolean hasOpenCrmUploads = customerRepositoryPort.isHasOpenCrmUploadsByEmail(emailAddress);
        boolean hasOpenDuplicateChecks = customerRepositoryPort.isHasOpenDuplicateChecksByEmail(emailAddress);
        Optional<CrmUploadCoreInfo> crmUploadInfo = customerRepositoryPort.findLatestUploadByEmail(emailAddress);
        List<String> products = customerRepositoryPort.findProductsByEmail(emailAddress);

        return new LoginResponse(token, enabled, hasOpenCrmUploads, hasOpenDuplicateChecks, crmUploadInfo.orElse(null), products);
    }
}
