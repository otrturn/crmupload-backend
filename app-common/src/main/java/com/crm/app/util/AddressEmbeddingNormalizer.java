package com.crm.app.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AddressEmbeddingNormalizer {

    private static final String LITERAL_STRASSE = "strasse";

    // Erkennt z.B. "12", "12a", "12 a", "12-14", "12/14"
    // bewusst "locker", weil User-Eingaben wild sind
    private static final Pattern HOUSE_NO =
            Pattern.compile("\\b(\\d{1,5})(?:\\s*([a-z]))?(?:\\s*[-/]\\s*(\\d{1,5}))?\\b");

    private AddressEmbeddingNormalizer() {
    }

    /**
     * streetAndHouseNo: ein Feld, z.B. "Hauptstr. 5a"
     * city: z.B. "Köln"
     * Rückgabe: "strasse hauptstrasse hausnummer 5 a ort koeln"
     */
    public static String normalize(String streetAndHouseNo, String city) {
        String line = normalizeText(streetAndHouseNo);

        // Straßen-Token vereinheitlichen
        line = line
                .replaceAll("\\bstraße\\b", LITERAL_STRASSE)
                .replaceAll("\\bstr\\.?\\b", LITERAL_STRASSE)
                .replaceAll("\\bstrasse\\b", LITERAL_STRASSE);

        // "nr" / "nr." entfernen (stört eher)
        line = line.replaceAll("\\bnr\\.?\\b", " ");

        // Hausnummer suchen (nimmt das erste passende Vorkommen; in 99% ok)
        String house = "";
        Matcher m = HOUSE_NO.matcher(line);
        if (m.find()) {
            String n1 = stripLeadingZeros(m.group(1));            // "05" -> "5"
            String letter = m.group(2) != null ? m.group(2) : ""; // "a"
            String n2 = m.group(3) != null ? stripLeadingZeros(m.group(3)) : ""; // bei "5-7"

            house = n1 + (letter.isEmpty() ? "" : " " + letter) + (n2.isEmpty() ? "" : " " + n2);

            // Hausnummer aus der Zeile entfernen, damit der Straßenname stabiler ist
            line = (line.substring(0, m.start()) + " " + line.substring(m.end())).trim();
            line = line.replaceAll("\\s+", " ");
        }

        String street = line.trim();
        String ort = normalizeText(city);

        // Schema-Text (PLZ bewusst NICHT drin)
        StringBuilder sb = new StringBuilder();
        if (!street.isBlank()) sb.append("strasse ").append(street).append(" ");
        if (!house.isBlank()) sb.append("hausnummer ").append(house).append(" ");
        if (!ort.isBlank()) sb.append("ort ").append(ort);

        return sb.toString().trim().replaceAll("\\s+", " ");
    }

    private static String normalizeText(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.GERMAN);
        t = Normalizer.normalize(t, Normalizer.Form.NFKC);

        // Umlaute vereinheitlichen (macht "Köln" und "Koeln" kompatibler)
        t = t.replace("ß", "ss")
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue");

        // Satzzeichen/Trenner -> Space
        t = t.replaceAll("[,.;:()\\[\\]{}|\\\\]+", " ");
        // Bindestriche innerhalb von Wörtern sind tricky: wir lassen sie als Space,
        // damit "haupt-strasse" nicht anders embedded wird
        t = t.replaceAll("-+", " ");

        return t.replaceAll("\\s+", " ").trim();
    }

    private static String stripLeadingZeros(String n) {
        return n.replaceFirst("^0+(?!$)", "");
    }
}
