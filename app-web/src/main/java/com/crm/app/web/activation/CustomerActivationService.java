package com.crm.app.web.activation;

import com.crm.app.port.customer.CustomerActivationRepositoryPort;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.web.config.AppWebProperties;
import com.crm.app.web.mail.ActivationMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerActivationService {

    private final CustomerActivationRepositoryPort activationRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ActivationMailService activationMailService;
    private final AppWebProperties appWebProperties;

    @Transactional
    public boolean activateByToken(String token) {
        Optional<Long> customerIdOpt = activationRepository.findValidCustomerIdByToken(token);
        if (customerIdOpt.isEmpty()) {
            return false;
        }

        long customerId = customerIdOpt.get();

        customerRepository.setEnabled(customerId, true);

        activationRepository.markTokenUsed(token);

        return true;
    }

    public void sendActivationEmail(String emailAddress, String name, Long customerId) {
        String activationToken = activationRepository.createActivationToken(customerId);

        String activationLink = appWebProperties.getBaseUrl() + appWebProperties.getUri() + "?token=" + activationToken;

        activationMailService.sendActivationMail(
                emailAddress,
                name,
                activationLink
        );


    }
}
