package com.crm.app.dto;

import java.sql.Timestamp;

public record CustomerProfile(String customerNumber, String firstname, String lastname, String companyName,
                              String emailAddress, String phoneNumber,
                              String adrline1, String adrline2, String postalcode, String city,
                              String country,
                              String taxId, String vatId,
                              Timestamp activationDate) {
}
