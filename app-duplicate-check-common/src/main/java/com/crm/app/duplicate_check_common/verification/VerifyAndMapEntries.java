package com.crm.app.duplicate_check_common.verification;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crmmacher.bexio_excel.dto.BexioColumn;
import com.crmmacher.bexio_excel.dto.BexioEntry;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.lexware_excel.dto.LexwareColumn;
import com.crmmacher.lexware_excel.dto.LexwareEntry;
import com.crmmacher.my_excel.dto.MyExcelAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifyAndMapEntries {

    private static final String LITERAL_FIRMENNAME_LEER = "[Account] Zeile %d: Firmenname ist leer";
    private static final String LITERAL_PLZ_LEER = "[Account] Zeile %d: PLZ ist leer";
    private static final String LITERAL_STRASSE_LEER = "[Account] Zeile %d: Strasse ist leer";
    private static final String LITERAL_LAND_LEER = "[Account] Zeile %d: Land ist leer";

    private VerifyAndMapEntries() {
    }

    public static List<DuplicateCheckEntry> verifyAndMapEntriesForMyExcel(List<MyExcelAccount> myExcelEntries, List<ErrMsg> errors) {
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

    public static List<DuplicateCheckEntry> verifyAndMapEntriesForLexware(List<LexwareEntry> lexwareEntries, Map<LexwareColumn, Integer> indexMap, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        boolean isSuccess = true;
        for (int i = 0; i < lexwareEntries.size(); i++) {
            LexwareEntry lexwareEntry = lexwareEntries.get(i);
            isSuccess = verifyEntryForLexware(indexMap, errors, lexwareEntry, i, isSuccess);
            if (isSuccess) {
                DuplicateCheckEntry duplicateCheckEntry = DuplicateCheckEntry.builder()
                        .cExternalReference(lexwareEntry.getcExternalReference())
                        .accountName(lexwareEntry.getAccountName())
                        .postalCode(lexwareEntry.getAddress().getPostcalCode())
                        .street(lexwareEntry.getAddress().getStreet())
                        .city(lexwareEntry.getAddress().getCity())
                        .country(lexwareEntry.getAddress().getCountry())
                        .emailAddress(
                                !lexwareEntry.getEmailAddressData().isEmpty()
                                        ? lexwareEntry.getEmailAddressData().get(0).getEmailAddress()
                                        : ""
                        )
                        .phoneNumber(
                                !lexwareEntry.getPhoneNumberData().isEmpty()
                                        ? lexwareEntry.getPhoneNumberData().get(0).getPhoneNumber()
                                        : ""
                        )
                        .build();

                duplicateCheckEntries.add(duplicateCheckEntry);
            }
        }
        return duplicateCheckEntries;
    }

    public static boolean verifyEntryForLexware(Map<LexwareColumn, Integer> indexMap, List<ErrMsg> errors, LexwareEntry lexwareEntry, int i, boolean isSuccess) {
        if (lexwareEntry.getAccountName() == null || lexwareEntry.getAccountName().isBlank()) {
            String msg = String.format(LITERAL_FIRMENNAME_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.FIRMENNAME), LexwareColumn.FIRMENNAME.name(), msg));
            isSuccess = false;
        }
        if (lexwareEntry.getAddress().getPostcalCode() == null || lexwareEntry.getAddress().getPostcalCode().isBlank()) {
            String msg = String.format(LITERAL_PLZ_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.PLZ), LexwareColumn.PLZ.name(), msg));
            isSuccess = false;
        }
        if (lexwareEntry.getAddress().getStreet() == null || lexwareEntry.getAddress().getStreet().isBlank()) {
            String msg = String.format(LITERAL_STRASSE_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.STRASSE), LexwareColumn.STRASSE.name(), msg));
            isSuccess = false;
        }
        if (lexwareEntry.getAddress().getCity() == null || lexwareEntry.getAddress().getCity().isBlank()) {
            String msg = String.format(LITERAL_LAND_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.LAND), LexwareColumn.LAND.name(), msg));
            isSuccess = false;
        }
        return isSuccess;
    }

    public static List<DuplicateCheckEntry> verifyAndMapEntriesForBexio(List<BexioEntry> bexioEntries, Map<BexioColumn, Integer> indexMap, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        for (int i = 0; i < bexioEntries.size(); i++) {
            BexioEntry bexioEntry = bexioEntries.get(i);
            if (bexioEntry.getAccountName() == null || bexioEntry.getAccountName().isBlank()) {
                String msg = String.format(LITERAL_FIRMENNAME_LEER, i + 1);
                errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.FIRMENNAME), BexioColumn.FIRMENNAME.name(), msg));
            } else if (bexioEntry.getAddress().getPostcalCode() == null || bexioEntry.getAddress().getPostcalCode().isBlank()) {
                String msg = String.format(LITERAL_PLZ_LEER, i + 1);
                errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.PLZ), BexioColumn.PLZ.name(), msg));
            } else {
                DuplicateCheckEntry duplicateCheckEntry = DuplicateCheckEntry.builder()
                        .cExternalReference(bexioEntry.getcExternalReference())
                        .accountName(bexioEntry.getAccountName())
                        .postalCode(bexioEntry.getAddress().getPostcalCode())
                        .street(bexioEntry.getAddress().getStreet())
                        .city(bexioEntry.getAddress().getCity())
                        .country(bexioEntry.getAddress().getCountry())
                        .emailAddress(
                                !bexioEntry.getEmailAddressData().isEmpty()
                                        ? bexioEntry.getEmailAddressData().get(0).getEmailAddress()
                                        : ""
                        )
                        .phoneNumber(
                                !bexioEntry.getPhoneNumberData().isEmpty()
                                        ? bexioEntry.getPhoneNumberData().get(0).getPhoneNumber()
                                        : ""
                        )
                        .build();
                duplicateCheckEntries.add(duplicateCheckEntry);
            }
        }
        return duplicateCheckEntries;
    }
}
