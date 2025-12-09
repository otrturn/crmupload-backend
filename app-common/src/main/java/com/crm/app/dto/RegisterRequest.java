package com.crm.app.dto;

import java.util.List;

public record RegisterRequest(String firstname, String lastname, String company_name, String email_address,
                              String phone_number,
                              String adrline1, String adrline2, String postalcode, String city, String country,
                              String password, List<String> products) {
}
