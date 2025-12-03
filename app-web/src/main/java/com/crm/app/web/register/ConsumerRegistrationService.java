package com.crm.app.web.register;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.dto.RegisterResponse;
import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import com.crm.app.web.activation.ConsumerActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsumerRegistrationService {

    private final UserAccountRegistrationService userAccountRegistrationService;
    private final ConsumerRepositoryPort consumerRepository;
    private final ConsumerActivationService consumerActivationService;

    @Transactional
    public ResponseEntity<RegisterResponse> registerConsumer(RegisterRequest request) {
        String emailAddress = request.email_address();

        if (consumerRepository.emailExists(emailAddress)) {
            throw new IllegalStateException("Consumer with email already exists: " + emailAddress);
        }

        UserAccountRegistrationResult accountResult =
                userAccountRegistrationService.registerUserAccount(emailAddress, request.password());

        long consumerId = consumerRepository.nextConsumerId();

        Consumer consumer = new Consumer(
                consumerId,
                accountResult.userId(),
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

        consumerActivationService.sendActivationEmail(
                emailAddress,
                request.firstname() + " " + request.lastname(),
                consumerId
        );

        return ResponseEntity
                .status(201)
                .body(new RegisterResponse(accountResult.jwtToken()));
    }
}
