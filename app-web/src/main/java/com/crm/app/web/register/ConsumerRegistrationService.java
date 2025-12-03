package com.crm.app.web.register;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import com.crm.app.port.user.UserAccount;
import com.crm.app.port.user.UserAccountRepositoryPort;
import com.crm.app.web.activation.ConsumerActivationService;
import com.crm.app.web.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumerRegistrationService {

    private final UserAccountRepositoryPort userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    private final ConsumerRepositoryPort consumerRepository;
    private final ConsumerActivationService consumerActivationService;

    @Transactional
    public ResponseEntity<RegisterResponse> registerConsumer(RegisterRequest request) {
        String emailAddress = request.email_address();

        if (userAccountRepository.existsByUsername(emailAddress)) {
            throw new IllegalStateException("Username already exists: " + emailAddress);
        }
        if (consumerRepository.emailExists(emailAddress)) {
            throw new IllegalStateException("Consumer with email already exists: " + emailAddress);
        }

        long userId = userAccountRepository.nextUserId();
        long consumerId = consumerRepository.nextConsumerId();

        String passwordHash = passwordEncoder.encode(request.password());

        UserAccount user = new UserAccount(
                userId,
                emailAddress,
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

        UserDetails userDetails = userDetailsService.loadUserByUsername(emailAddress);
        String token = jwtService.generateToken(userDetails);

        consumerActivationService.sendActivationEmail(emailAddress,request.firstname() + " " + request.lastname(),consumerId);

        return ResponseEntity
                .status(201)
                .body(new RegisterResponse(token));
    }
}