package com.crm.app.port.consumer;

import com.crm.app.dto.*;

import java.util.List;
import java.util.Optional;

public interface ConsumerRepositoryPort {

    boolean emailExists(String emailAddress);

    long nextConsumerId();

    void insertConsumer(Consumer consumer);

    boolean isEnabledByEmail(String emailAddress);

    boolean isEnabledByConsumerId(long consumerId);

    boolean isHasOpenUploadsByEmail(String emailAddress);

    boolean isHasOpenUploadsByConsumerId(long consumerId);

    void setEnabled(long consumerId, boolean enabled);

    ConsumerProfileResponse getConsumer(String emailAddress);

    int updateConsumerProfile(String emailAddress, ConsumerProfileRequest request);

    int updateConsumerPassword(String emailAddress, UpdatePasswordRequest request);

    List<ConsumerUploadHistory> findUploadHistoryByEmailAddress(String emailAddress);

    Optional<ConsumerUploadInfo> findLatestByConsumerId(long consumerId);

    Optional<ConsumerUploadInfo> findLatestByEmail(String email);

}