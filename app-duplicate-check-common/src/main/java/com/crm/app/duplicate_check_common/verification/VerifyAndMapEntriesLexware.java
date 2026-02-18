package com.crm.app.duplicate_check_common.verification;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.lexware_excel.dto.LexwareColumn;
import com.crmmacher.lexware_excel.dto.LexwareEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifyAndMapEntriesLexware {

    private static final String LITERAL_FIRMENNAME_LEER = "[Account] Zeile %d: Firmenname ist leer";
    private static final String LITERAL_PLZ_LEER = "[Account] Zeile %d: PLZ ist leer";
    private static final String LITERAL_STRASSE_LEER = "[Account] Zeile %d: Strasse ist leer";
    private static final String LITERAL_ORT_LEER = "[Account] Zeile %d: STADT ist leer";
    private static final String LITERAL_LAND_LEER = "[Account] Zeile %d: Land ist leer";

    private VerifyAndMapEntriesLexware() {
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
            String msg = String.format(LITERAL_ORT_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.ORT), LexwareColumn.ORT.name(), msg));
            isSuccess = false;
        }
        if (lexwareEntry.getAddress().getCity() == null || lexwareEntry.getAddress().getCity().isBlank()) {
            String msg = String.format(LITERAL_LAND_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.LAND), LexwareColumn.LAND.name(), msg));
            isSuccess = false;
        }
        return isSuccess;
    }

}
