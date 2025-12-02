package com.crm.app.port.consumer;

public interface ConsumerRepositoryPort {

    boolean emailExists(String emailAddress);

    long nextConsumerId();

    void insertConsumer(Consumer consumer);

    boolean isEnabledByEmail(String emailAddress);

    boolean isHasOpenUploads(String emailAddress);

    void setEnabled(long consumerId, boolean enabled);
}