package com.crm.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

    public RegisterRequest(RegisterRequest other) {
        this.firstname = other.firstname;
        this.lastname = other.lastname;
        this.companyName = other.companyName;
        this.emailAddress = other.emailAddress;
        this.phoneNumber = other.phoneNumber;
        this.adrline1 = other.adrline1;
        this.adrline2 = other.adrline2;
        this.postalcode = other.postalcode;
        this.city = other.city;
        this.country = other.country;
        this.taxId = other.taxId;
        this.vatId = other.vatId;
        this.password = other.password;

        this.products = (other.products == null) ? List.of() : new ArrayList<>(other.products);

        this.agbAccepted = other.agbAccepted;
        this.isEntrepreneur = other.isEntrepreneur;
        this.requestImmediateServiceStart = other.requestImmediateServiceStart;
        this.acknowledgeWithdrawalLoss = other.acknowledgeWithdrawalLoss;
        this.termsVersion = other.termsVersion;
    }

}