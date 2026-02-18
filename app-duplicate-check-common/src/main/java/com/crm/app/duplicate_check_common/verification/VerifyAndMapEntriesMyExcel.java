package com.crm.app.duplicate_check_common.verification;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.my_excel.dto.MyExcelAccount;

import java.util.ArrayList;
import java.util.List;

public class VerifyAndMapEntriesMyExcel {

    private static final String LITERAL_FIRMENNAME_LEER = "[Account] Zeile %d: Firmenname ist leer";
    private static final String LITERAL_PLZ_LEER = "[Account] Zeile %d: PLZ ist leer";
    private static final String LITERAL_STRASSE_LEER = "[Account] Zeile %d: Strasse ist leer";
    private static final String LITERAL_LAND_LEER = "[Account] Zeile %d: Land ist leer";

    private VerifyAndMapEntriesMyExcel() {
    }

    public static List<DuplicateCheckEntry> verifyAndMapEntriesForMyExcelAccounts(List<MyExcelAccount> myExcelEntries, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        for (int i = 0; i < myExcelEntries.size(); i++) {
            MyExcelAccount myExcelEntry = myExcelEntries.get(i);
            if (myExcelEntry.getName() == null || myExcelEntry.getName().isBlank()) {
                String msg = String.format(LITERAL_FIRMENNAME_LEER, i + 1);
                errors.add(new ErrMsg(0, i, 0, "Firmenname", msg));
            } else if (myExcelEntry.getBillingAddress().getPostcalCode() == null || myExcelEntry.getBillingAddress().getPostcalCode().isBlank()) {
                String msg = String.format(LITERAL_PLZ_LEER, i + 1);
                errors.add(new ErrMsg(0, i, 0, "PLZ", msg));
            } else {
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

}
