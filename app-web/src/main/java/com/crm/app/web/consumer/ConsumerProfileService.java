package com.crm.app.web.consumer;


import com.crm.app.dto.ConsumerProfileRequest;
import com.crm.app.dto.ConsumerProfileResponse;
import com.crm.app.dto.UpdatePasswordRequest;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import com.crm.app.web.error.ConsumerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumerProfileService {

    private final ConsumerRepositoryPort consumerRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public ConsumerProfileResponse getConsumerByEmail(String emailAddress) {
        ConsumerProfileResponse response = consumerRepositoryPort.getConsumer(emailAddress);
        if (response == null) {
            throw new ConsumerNotFoundException(emailAddress);
        }
        return response;
    }

    public void updateConsumerProfile(String email, ConsumerProfileRequest request) {
        int rows = consumerRepositoryPort.updateConsumerProfile(email, request);
        if (rows == 0) {
            throw new ConsumerNotFoundException(request.email_address());
        }
    }

    public void updateConsumerPassword(String emailAddress, UpdatePasswordRequest request) {

        String hash = passwordEncoder.encode(request.password());
        UpdatePasswordRequest passwordHash = new UpdatePasswordRequest(hash);
        int rows = consumerRepositoryPort.updateConsumerPassword(emailAddress, passwordHash);

        if (rows == 0) {
            throw new ConsumerNotFoundException(emailAddress);
        }
    }
}
