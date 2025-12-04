package com.crm.app.port.consumer;

public interface ConsumerRepositoryPort {

    boolean emailExists(String emailAddress);

    long nextConsumerId();

    void insertConsumer(Consumer consumer);

    boolean isEnabledByEmail(String emailAddress);

    boolean isEnabledByConsumerId(long consumerId);

    boolean isHasOpenUploadsByEmail(String emailAddress);

    boolean isHasOpenUploadsByConsumerId(long consumerId);

    void setEnabled(long consumerId, boolean enabled);
}