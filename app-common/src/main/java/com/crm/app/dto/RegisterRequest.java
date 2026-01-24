package com.crm.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String companyName;
    private String emailAddress;

    private String phoneNumber;

    private String adrline1;
    private String adrline2;
    private String postalcode;
    private String city;
    private String country;

    private String taxId;
    private String vatId;

    private String password;
    private List<String> products;

    private boolean agbAccepted;
    private boolean isEntrepreneur;
    private boolean requestImmediateServiceStart;
    private boolean acknowledgeWithdrawalLoss;

    private String termsVersion;

}