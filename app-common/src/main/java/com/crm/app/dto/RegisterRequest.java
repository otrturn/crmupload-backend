package com.crm.app.dto;

import java.util.List;

public record RegisterRequest(String firstname, String lastname, String companyName, String emailAddress,
                              String phoneNumber,
                              String adrline1, String adrline2, String postalcode, String city, String country,
                              String taxId, String vatId,
                              String password, List<String> products,
                              boolean agbAccepted,
                              boolean isEntrepreneur,
                              boolean requestImmediateServiceStart,
                              boolean acknowledgeWithdrawalLoss,
                              String termsVersion
) {
}
