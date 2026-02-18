package com.crm.app.duplicate_check_common.verification;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crmmacher.bexio_excel.dto.BexioColumn;
import com.crmmacher.bexio_excel.dto.BexioEntry;
import com.crmmacher.error.ErrMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifyAndMapEntriesBexio {

    private static final String LITERAL_FIRMENNAME_LEER = "[Account] Zeile %d: Firmenname ist leer";
    private static final String LITERAL_PLZ_LEER = "[Account] Zeile %d: PLZ ist leer";
    private static final String LITERAL_STRASSE_LEER = "[Account] Zeile %d: Adresse ist leer";
    private static final String LITERAL_ORT_LEER = "[Account] Zeile %d: STADT ist leer";
    private static final String LITERAL_LAND_LEER = "[Account] Zeile %d: Land ist leer";

    private VerifyAndMapEntriesBexio() {
    }

    public static List<DuplicateCheckEntry> verifyAndMapEntriesForBexio(List<BexioEntry> bexioEntries, Map<BexioColumn, Integer> indexMap, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        boolean isSuccess = true;
        for (int i = 0; i < bexioEntries.size(); i++) {
            BexioEntry bexioEntry = bexioEntries.get(i);
            isSuccess = verifyEntryForBexio(indexMap, errors, bexioEntry, i, isSuccess);
            if (isSuccess) {
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

    public static boolean verifyEntryForBexio(Map<BexioColumn, Integer> indexMap, List<ErrMsg> errors, BexioEntry bexioEntry, int i, boolean isSuccess) {
        if (bexioEntry.getAccountName() == null || bexioEntry.getAccountName().isBlank()) {
            String msg = String.format(LITERAL_FIRMENNAME_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.FIRMENNAME), BexioColumn.FIRMENNAME.name(), msg));
            isSuccess = false;
        }
        if (bexioEntry.getAddress().getPostcalCode() == null || bexioEntry.getAddress().getPostcalCode().isBlank()) {
            String msg = String.format(LITERAL_PLZ_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.PLZ), BexioColumn.PLZ.name(), msg));
            isSuccess = false;
        }
        if (bexioEntry.getAddress().getStreet() == null || bexioEntry.getAddress().getStreet().isBlank()) {
            String msg = String.format(LITERAL_STRASSE_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.ADRESSE), BexioColumn.ADRESSE.name(), msg));
            isSuccess = false;
        }
        if (bexioEntry.getAddress().getCity() == null || bexioEntry.getAddress().getCity().isBlank()) {
            String msg = String.format(LITERAL_ORT_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.ORT), BexioColumn.ORT.name(), msg));
            isSuccess = false;
        }
        if (bexioEntry.getAddress().getCountry() == null || bexioEntry.getAddress().getCountry().isBlank()) {
            String msg = String.format(LITERAL_LAND_LEER, i + 1);
            errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.LAND), BexioColumn.LAND.name(), msg));
            isSuccess = false;
        }
        return isSuccess;
    }
}
