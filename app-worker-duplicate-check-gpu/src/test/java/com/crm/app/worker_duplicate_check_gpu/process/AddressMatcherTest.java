package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.duplicate_check_common.dto.AddressMatchCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressMatcherTest {

    @Test
    void exactSame_shouldMatch() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße 5", "Köln");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße 5", "Köln");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        assertEquals(AddressMatchCategory.MATCH, r.category());
        assertTrue(r.score() >= 0.95);
    }

    @Test
    void umlautAndStrAbbrev_shouldMatch() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstr. 5a", "Köln");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße 5 A", "Koeln");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        assertEquals(AddressMatchCategory.MATCH, r.category());
        assertTrue(r.houseSim() >= 0.70);
        assertTrue(r.citySim() >= 0.90);
    }

    @Test
    void houseNumberDifferent_sameStreetCity_shouldNoMatch() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Bahnhofstraße 12", "München");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Bahnhofstr 14", "Muenchen");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        assertEquals(AddressMatchCategory.NO_MATCH, r.category());
    }

    @Test
    void houseNumberRange_shouldBePossibleOrMatch() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Bahnhofstraße 12-14", "München");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Bahnhofstr 13", "Muenchen");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        // je nach Schreibvariante kann das MATCH oder POSSIBLE werden – beides ok
        assertTrue(r.category() == AddressMatchCategory.MATCH
                || r.category() == AddressMatchCategory.POSSIBLE);
        assertTrue(r.houseSim() >= 0.70);
    }

    @Test
    void cityAbbrev_shouldNotFailGate() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Zeil 10", "Frankfurt am Main");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Zeil 10", "Frankfurt a. M.");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        assertTrue(r.citySim() >= 0.90);
        assertTrue(r.category() == AddressMatchCategory.MATCH
                || r.category() == AddressMatchCategory.POSSIBLE);
    }

    @Test
    void districtVsCity_shouldBePossibleNotNoMatch() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße 5", "Köln");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstr 5", "Köln-Porz");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        // je nach Daten willst du hier eher POSSIBLE als NO_MATCH
        assertNotEquals(AddressMatchCategory.NO_MATCH, r.category());
    }

    @Test
    void missingHouseNumber_oneSide_shouldUsuallyBePossible() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße", "Köln");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße 5", "Köln");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        // wegen einseitig fehlender Hausnummer: nicht direkt MATCH (Default)
        assertTrue(r.category() == AddressMatchCategory.POSSIBLE
                || r.category() == AddressMatchCategory.NO_MATCH);
    }

    @Test
    void differentCity_shouldNoMatchEvenIfStreetLooksSimilar() {
        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße 5", "Köln");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße 5", "Berlin");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b);
        assertEquals(AddressMatchCategory.NO_MATCH, r.category());
    }

    @Test
    void config_canMakeItStricter_againstFalsePositives() {
        var strict = new com.crm.app.duplicate_check_common.matcher.AddressMatcher.MatchConfig(
                0.92, // cityGate strenger
                0.90, // MATCH strenger
                0.84, // POSSIBLE strenger
                0.95, // house missing -> street muss extrem stark sein
                0.45, 0.35, 0.20
        );

        var a = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstraße", "Köln");
        var b = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of("Hauptstr. 5", "Koeln");

        var r = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(a, b, strict);
        // strenger Config: eher NO_MATCH/POSSIBLE
        assertNotEquals(AddressMatchCategory.MATCH, r.category());
    }
}
