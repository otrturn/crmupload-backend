package com.crm.app.port.customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerActivationRepositoryPort {
    String createActivationToken(long customerId);

    Optional<Long> findValidCustomerIdByToken(String token);

    void markTokenUsed(String token);

    Optional<UUID> getTokenByEmail(String email);
}

