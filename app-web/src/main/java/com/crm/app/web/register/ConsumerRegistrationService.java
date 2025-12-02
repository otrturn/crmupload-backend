package com.crm.app.web.register;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerActivationRepositoryPort;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import com.crm.app.port.user.UserAccount;
import com.crm.app.port.user.UserAccountRepositoryPort;
import com.crm.app.web.mail.ActivationMailService;
import com.crm.app.web.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsumerRegistrationService {

    private final UserAccountRepositoryPort userAccountRepository;
    private final ConsumerRepositoryPort consumerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final ConsumerActivationRepositoryPort activationRepository;
    private final ActivationMailService activationMailService;

    @Value("${app.activation.base-url:http://localhost:8086}")
    private String activationBaseUrl;

    public ConsumerRegistrationService(UserAccountRepositoryPort userAccountRepository,
                                       ConsumerRepositoryPort consumerRepository,
                                       PasswordEncoder passwordEncoder,
                                       UserDetailsService userDetailsService,
                                       JwtService jwtService, ConsumerActivationRepositoryPort activationRepository, ActivationMailService activationMailService) {
        this.userAccountRepository = userAccountRepository;
        this.consumerRepository = consumerRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.activationRepository = activationRepository;
        this.activationMailService = activationMailService;
    }

    @Transactional
    public ResponseEntity<RegisterResponse> registerConsumer(RegisterRequest request) {
        String email = request.email_address();

        if (userAccountRepository.existsByUsername(email)) {
            throw new IllegalStateException("Username already exists: " + email);
        }
        if (consumerRepository.emailExists(email)) {
            throw new IllegalStateException("Consumer with email already exists: " + email);
        }

        long userId = userAccountRepository.nextUserId();
        long consumerId = consumerRepository.nextConsumerId();

        String passwordHash = passwordEncoder.encode(request.password());

        UserAccount user = new UserAccount(
                userId,
                email,
                passwordHash,
                List.of("ROLE_USER")
        );
        userAccountRepository.insertUserAccount(user);

        Consumer consumer = new Consumer(
                consumerId,
                userId,
                request.firstname(),
                request.lastname(),
                request.email_address(),
                request.phone_number(),
                request.adrline1(),
                request.adrline2(),
                request.postalcode(),
                request.city(),
                request.country()
        );
        consumerRepository.insertConsumer(consumer);

        String activationToken = activationRepository.createActivationToken(consumerId);

        String activationLink = activationBaseUrl + "/auth/activate?token=" + activationToken;

        activationMailService.sendActivationMail(
                request.email_address(),
                request.firstname() + " " + request.lastname(),
                activationLink
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity
                .status(201)
                .body(new RegisterResponse(token));
    }
}