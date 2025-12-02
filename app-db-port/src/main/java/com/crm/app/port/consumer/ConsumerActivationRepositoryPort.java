package com.crm.app.port.consumer;

import java.util.Optional;

public interface ConsumerActivationRepositoryPort {
    String createActivationToken(long consumerId);

    Optional<Long> findValidConsumerIdByToken(String token);

    void markTokenUsed(String token);
}

