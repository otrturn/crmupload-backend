package com.crm.app.port.customer;

import java.util.List;

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
        String country,
        List<String> products
) {

    public static String getFullname(Customer customer) {
        if (customer == null) {
            return "(Kein Name)";
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

