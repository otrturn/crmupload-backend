package com.crm.app.duplicate_check_common.verification;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.my_excel.dto.MyExcelAccount;
import com.crmmacher.my_excel.dto.MyExcelAccountColumn;

import java.util.ArrayList;
import java.util.List;

public class VerifyAndMapEntriesMyExcel {

    private static final String LITERAL_FIRMENNAME_LEER = "[Account] Zeile %d: Firmenname ist leer";
    private static final String LITERAL_PLZ_LEER = "[Account] Zeile %d: PLZ ist leer";
    private static final String LITERAL_STRASSE_LEER = "[Account] Zeile %d: Strasse ist leer";
    private static final String LITERAL_ORT_LEER = "[Account] Zeile %d: STADT ist leer";
    private static final String LITERAL_LAND_LEER = "[Account] Zeile %d: Land ist leer";

    private VerifyAndMapEntriesMyExcel() {
    }

    public static List<DuplicateCheckEntry> verifyAndMapEntriesForMyExcelAccounts(List<MyExcelAccount> myExcelEntries, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        boolean isSuccess = true;
        for (int i = 0; i < myExcelEntries.size(); i++) {
            MyExcelAccount myExcelEntry = myExcelEntries.get(i);
            isSuccess = verifyEntryForMyExcelAccount(errors, myExcelEntry, i, isSuccess);
            if (isSuccess) {
                DuplicateCheckEntry duplicateCheckEntry = DuplicateCheckEntry.builder()
                        .cExternalReference(myExcelEntry.getcExternalReference())
                        .accountName(myExcelEntry.getName())
                        .postalCode(myExcelEntry.getBillingAddress().getPostcalCode())
                        .street(myExcelEntry.getBillingAddress().getStreet())
                        .city(myExcelEntry.getBillingAddress().getCity())
                        .country(myExcelEntry.getBillingAddress().getCountry())
                        .emailAddress(
                                !myExcelEntry.getEmailAddressData().isEmpty()
                                        ? myExcelEntry.getEmailAddressData().get(0).getEmailAddress()
                                        : ""
                        )
                        .phoneNumber(
                                !myExcelEntry.getPhoneNumberData().isEmpty()
                                        ? myExcelEntry.getPhoneNumberData().get(0).getPhoneNumber()
                                        : ""
                        )
                        .build();

                duplicateCheckEntries.add(duplicateCheckEntry);
            }
        }
        return duplicateCheckEntries;
    }

    public static boolean verifyEntryForMyExcelAccount(List<ErrMsg> errors, MyExcelAccount myExcelEntry, int i, boolean isSuccess) {
        if (myExcelEntry.getName() == null || myExcelEntry.getName().isBlank()) {
            String msg = String.format(LITERAL_FIRMENNAME_LEER, i + 1);
            errors.add(new ErrMsg(0, i, MyExcelAccountColumn.NAME.index(), MyExcelAccountColumn.NAME.header(), msg));
            isSuccess = false;
        }
        if (myExcelEntry.getBillingAddress().getPostcalCode() == null || myExcelEntry.getBillingAddress().getPostcalCode().isBlank()) {
            String msg = String.format(LITERAL_PLZ_LEER, i + 1);
            errors.add(new ErrMsg(0, i, MyExcelAccountColumn.BILLINGADDRESS_PLZ.index(), MyExcelAccountColumn.BILLINGADDRESS_PLZ.header(), msg));
            isSuccess = false;
        }
        if (myExcelEntry.getBillingAddress().getStreet() == null || myExcelEntry.getBillingAddress().getStreet().isBlank()) {
            String msg = String.format(LITERAL_STRASSE_LEER, i + 1);
            errors.add(new ErrMsg(0, i, MyExcelAccountColumn.BILLINGADDRESS_STRASSE.index(), MyExcelAccountColumn.BILLINGADDRESS_STRASSE.header(), msg));
            isSuccess = false;
        }
        if (myExcelEntry.getBillingAddress().getCity() == null || myExcelEntry.getBillingAddress().getCity().isBlank()) {
            String msg = String.format(LITERAL_ORT_LEER, i + 1);
            errors.add(new ErrMsg(0, i, MyExcelAccountColumn.BILLINGADDRESS_ORT.index(), MyExcelAccountColumn.BILLINGADDRESS_ORT.header(), msg));
            isSuccess = false;
        }
        if (myExcelEntry.getBillingAddress().getCountry() == null || myExcelEntry.getBillingAddress().getCountry().isBlank()) {
            String msg = String.format(LITERAL_LAND_LEER, i + 1);
            errors.add(new ErrMsg(0, i, MyExcelAccountColumn.BILLINGADDRESS_LAND.index(), MyExcelAccountColumn.BILLINGADDRESS_LAND.header(), msg));
            isSuccess = false;
        }
        return isSuccess;
    }
}
