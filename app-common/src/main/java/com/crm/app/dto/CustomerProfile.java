package com.crm.app.dto;

import java.sql.Timestamp;

public record CustomerProfile(String customer_number, String firstname, String lastname, String company_name,
                              String email_address, String phone_number,
                              String adrline1, String adrline2, String postalcode, String city,
                              String country,
                              String tax_id, String vat_id,
                              Timestamp activation_date) {
}
