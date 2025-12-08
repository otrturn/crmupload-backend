package com.crm.app.dto;

public record CustomerProfileResponse(String firstname, String lastname, String company_name,
                                      String email_address, String phone_number,
                                      String adrline1, String adrline2, String postalcode, String city,
                                      String country) {
}
