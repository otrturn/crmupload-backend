package com.crm.app.web.register;

import com.crm.app.port.consumer.ConsumerActivationRepositoryPort;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivationService {

    private final ConsumerActivationRepositoryPort activationRepository;
    private final ConsumerRepositoryPort consumerRepository;

    @Transactional
    public boolean activateByToken(String token) {
        Optional<Long> consumerIdOpt = activationRepository.findValidConsumerIdByToken(token);
        if (consumerIdOpt.isEmpty()) {
            return false;
        }

        long consumerId = consumerIdOpt.get();

        // Consumer auf enabled = TRUE setzen
        consumerRepository.setEnabled(consumerId, true);

        // Token als genutzt markieren
        activationRepository.markTokenUsed(token);

        return true;
    }
}
