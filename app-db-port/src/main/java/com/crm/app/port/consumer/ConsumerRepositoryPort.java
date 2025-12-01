package com.crm.app.port.consumer;

public interface ConsumerRepositoryPort {

    boolean emailExists(String emailAddress);

    long nextConsumerId();

    void insertConsumer(Consumer consumer);
}