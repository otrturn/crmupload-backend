package com.crm.app.dto;

import java.sql.Timestamp;
import java.util.List;

public record Customer(
        Long customerId,
        String customerNumber,
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
        String country,
        List<String> products,
        Timestamp activationDate
) {

    public static String getFullname(Customer customer) {
        if (customer == null) {
            return "";
        }
        String firstName = "";
        if (customer.firstname() != null && !customer.firstname().isEmpty()) {
            firstName = customer.firstname();
        }
        String lastName = "";
        if (customer.lastname() != null && !customer.lastname().isEmpty()) {
            lastName = customer.lastname();
        }
        return (!firstName.isEmpty() ? firstName + " " : "") + lastName;
    }
}

