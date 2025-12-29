package com.crm.app.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class DuplicateCheckEntry {
    private String cExternalReference;
    private String accountName;
    private String postalCode;
    private String street;
    private String city;
    private String country;
    private String emailAddress;
    private String phoneNumber;

    public DuplicateCheckEntry(String cExternalReference, String accountName, String postalCode, String street, String city, String country, String emailAddress, String phoneNumber) {
        this.cExternalReference = cExternalReference;
        this.accountName = accountName;
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
        this.country = country;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }
}
