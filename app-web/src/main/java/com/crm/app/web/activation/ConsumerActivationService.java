package com.crm.app.web.activation;

import com.crm.app.port.consumer.ConsumerActivationRepositoryPort;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import com.crm.app.web.config.AppWebProperties;
import com.crm.app.web.mail.ActivationMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsumerActivationService {

    private final ConsumerActivationRepositoryPort activationRepository;
    private final ConsumerRepositoryPort consumerRepository;
    private final ActivationMailService activationMailService;
    private final AppWebProperties appWebProperties;

    @Transactional
    public boolean activateByToken(String token) {
        Optional<Long> consumerIdOpt = activationRepository.findValidConsumerIdByToken(token);
        if (consumerIdOpt.isEmpty()) {
            return false;
        }

        long consumerId = consumerIdOpt.get();

        consumerRepository.setEnabled(consumerId, true);

        activationRepository.markTokenUsed(token);

        return true;
    }

    public void sendActivationEmail(String emailAddress, String name, Long consumerId) {
        String activationToken = activationRepository.createActivationToken(consumerId);

        String activationLink = appWebProperties.getBaseUrl() + appWebProperties.getUri() + "?token=" + activationToken;

        activationMailService.sendActivationMail(
                emailAddress,
                name,
                activationLink
        );


    }
}
