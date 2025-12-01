package com.crm.app.port.consumer;

public record Consumer(
        Long consumerId,
        Long userId,
        String firstname,
        String lastname,
        String emailAddress,
        String phoneNumber,
        String adrline1,
        String adrline2,
        String postalcode,
        String city,
        String country
) {}

