package com.crm.app.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckEntry {

    private String cExternalReference;
    private String accountName;
    private String postalCode;
    private String street;
    private String city;
    private String country;
    private String emailAddress;
    private String phoneNumber;
}
