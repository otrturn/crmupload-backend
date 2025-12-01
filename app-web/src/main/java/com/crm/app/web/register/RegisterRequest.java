package com.crm.app.web.register;

public record RegisterRequest(String firstname, String lastname, String email_address, String phone_number,
                              String adrline1, String adrline2, String postalcode, String city, String country, String password) {
}
