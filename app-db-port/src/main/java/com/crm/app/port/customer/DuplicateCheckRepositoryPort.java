package com.crm.app.port.customer;

import com.crm.app.dto.DuplicateCheckRequest;

import java.util.List;

public interface DuplicateCheckRepositoryPort {
    long nextDuplicateCheckId();

    void insertDuplicateCheck(DuplicateCheckRequest duplicateCheckRequest);

    List<Long> claimNextDuplicateChecksForCheck(int limit);

    void markDuplicateCheckChecked(long uploadId, byte[] content);

    void markDuplicateCheckFailed(long uploadId, String errorMessage);

}