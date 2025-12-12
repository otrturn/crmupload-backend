package com.crm.app.port.customer;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckRequest;

import java.util.List;

public interface DuplicateCheckRepositoryPort {
    long nextDuplicateCheckId();

    void insertDuplicateCheck(DuplicateCheckRequest duplicateCheckRequest);

    List<Long> claimNextDuplicateChecksForVerification(int limit);

    List<Long> claimNextDuplicateChecksForFinalisation(int limit);

    List<Long> claimNextDuplicateChecksForCheck(int limit);

    void markDuplicateCheckVerified(long uploadId, byte[] content);

    void markDuplicateCheckChecked(long uploadId, byte[] content);

    void markDuplicateCheckFailed(long uploadId, String errorMessage);

    void markDuplicateCheckDone(long uploadId);

    List<DuplicateCheckContent> findDuplicateChecksByIds(List<Long> duplicateCheckIds);
}