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
    private static final String LITERAL_STRASSE_LEER = "[Account] Zeile %d: Strasse ist leer";
    private static final String LITERAL_LAND_LEER = "[Account] Zeile %d: Land ist leer";

    private VerifyAndMapEntriesBexio() {
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
