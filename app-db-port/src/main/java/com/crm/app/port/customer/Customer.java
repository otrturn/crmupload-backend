package com.crm.app.port.customer;

public record Customer(
        Long customerId,
        Long userId,
        String firstname,
        String lastname,
        String companyName,
        String emailAddress,
        String phoneNumber,
        String adrline1,
        String adrline2,
        String postalcode,
        String city,
        String country
) {
}

