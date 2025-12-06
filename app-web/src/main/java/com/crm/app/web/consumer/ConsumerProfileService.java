package com.crm.app.web.consumer;


import com.crm.app.dto.ConsumerProfileResponse;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import com.crm.app.web.error.ConsumerNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ConsumerProfileService {

    private final ConsumerRepositoryPort consumerRepositoryPort;

    public ConsumerProfileService(ConsumerRepositoryPort consumerRepositoryPort) {
        this.consumerRepositoryPort = consumerRepositoryPort;
    }

    public ConsumerProfileResponse getCustomerByEmail(String emailAddress) {
        ConsumerProfileResponse response = consumerRepositoryPort.getConsumer(emailAddress);
        if (response == null) {
            throw new ConsumerNotFoundException(emailAddress);
        }
        return response;
    }
}
