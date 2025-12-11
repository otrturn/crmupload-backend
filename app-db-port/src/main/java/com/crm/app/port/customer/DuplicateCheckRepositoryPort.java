package com.crm.app.port.customer;

import com.crm.app.dto.DuplicateCheckRequest;

public interface DuplicateCheckRepositoryPort {
    long nextDuplicateCheckId();

    void insertDuplicateCheck(
            DuplicateCheckRequest duplicateCheckRequest
    );
}