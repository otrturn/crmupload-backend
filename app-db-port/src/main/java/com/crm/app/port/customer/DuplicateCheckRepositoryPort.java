package com.crm.app.port.customer;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckRequest;

import java.util.List;

public interface DuplicateCheckRepositoryPort {
    long nextDuplicateCheckId();

    void insertDuplicateCheck(DuplicateCheckRequest duplicateCheckRequest);

    List<Long> claimNextDuplicateChecksForVerification(int limit);

    void markDuplicateCheckVerified(long uploadId, byte[] content);

    void markDuplicateCheckFailed(long uploadId, String errorMessage);

    List<DuplicateCheckContent> findDuplicateChecksByIds(List<Long> duplicateCheckIds);
}