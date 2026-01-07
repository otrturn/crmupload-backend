package com.crm.app.billing.dto;

public class BillingConstants {
    private BillingConstants() {
    }

    public static final String NAME_OF_COMPANY = "Ralf Scholler";
    public static final String STREET_OF_COMPANY = "Am Dorfplatz 6";
    public static final String POSTALCODE_OF_COMPANY = "57610";
    public static final String CITY_OF_COMPANY = "Ingelbach";
    public static final String ZIP_CITY_OF_COMPANY = POSTALCODE_OF_COMPANY + " " + CITY_OF_COMPANY;
    public static final String COUNTRY_CODE_OF_COMPANY = "DE";
    public static final String TAX_NUMBER_OF_COMPANY = "02/227/05080";
    public static final String DISPLAY_TEXT_TAX_NUMBER_OF_COMPANY = "Steuernummer " + TAX_NUMBER_OF_COMPANY;
    public static final String VAT_ID_NUMBER_OF_COMPANY = "DE244334416";
    public static final String DISPLAY_TEXT_VAT_ID_NUMBER_OF_COMPANY = "DE 244 3344 16";

    public static final String SUPPORT_EMAIL = "support@crmupload.de";
    public static final String CONTACT_NAME = "CRM-Upload Support";
    public static final String CONTACT_PHONE = "0170 2934 959";
    public static final String WEBSITE = "www.crmupload.de";

    public static final String BANK_NAME = "Raiffeisenbank Grävenwiesbach eG";
    public static final String BANK_ACCOUNT_NAME = "Ralf Scholler";
    public static final String BANK_IBAN = "DE59 5006 9345 0100 0362 77";
    public static final String BANK_BIC = "GENODE51GWB";


}
