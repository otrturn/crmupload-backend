package com.crm.app.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class DuplicateCheckEntry {
    private final String accountName;
    private final String postalCode;
    private final String street;
    private final String city;
    private final String country;
    private final String emailAddress;
    private final String phoneNumber;
}
