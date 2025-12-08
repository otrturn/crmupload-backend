package com.crm.app.port.customer;

import java.util.Optional;

public interface CustomerActivationRepositoryPort {
    String createActivationToken(long customerId);

    Optional<Long> findValidCustomerIdByToken(String token);

    void markTokenUsed(String token);
}

