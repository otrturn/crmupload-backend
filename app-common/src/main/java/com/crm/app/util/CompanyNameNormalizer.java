package com.crm.app.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class CompanyNameNormalizer {

    private CompanyNameNormalizer() {
    }

    private static final Pattern SEPARATORS_TO_SPACE =
            Pattern.compile("[\\p{Punct}·•–—_/\\\\+()\\[\\]{}'\"`´^°|@#=:;]+");

    private static final Pattern NON_ALNUM_SPACE = Pattern.compile("[^a-z0-9 ]+");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    // EXTRA #2: & / und / u. vereinheitlichen (wir mappen alles auf Space)
    private static final Pattern AND_CONNECTORS =
            Pattern.compile("\\b(&|und|u\\.)\\b");

    // Token-Stopwords (Rechtsformen). Alles lowercase, weil wir vorher lowercasen.
    private static final Set<String> LEGAL_FORM_TOKENS = Set.of(
            "gmbh", "ag", "kg", "ohg", "gbr", "ug", "ek", "ev", "kgaa", "se",
            "llc", "inc", "ltd", "limited", "corp", "corporation", "co", "company", "plc",
            "sarl", "sa", "spa", "oy", "ab", "as", "aps", "bv", "nv", "sro"
    );

    // EXTRA #1: Standort-/Org-Zusätze, die bei Dubletten stören
    private static final Set<String> ORG_NOISE_TOKENS = Set.of(
            "zweigniederlassung", "zweigniederlassungen",
            "niederlassung", "niederlassungen",
            "filiale", "filialen",
            "standort", "standorte",
            "betrieb", "betriebe"
    );

    // Mehrwort-Konstrukte & Sonder-Schreibweisen vor Token-Filter entfernen
    private static final List<Pattern> LEGAL_FORM_PHRASES = List.of(
            Pattern.compile("\\bgmbh\\s*(und|&)\\s*co\\b"),
            Pattern.compile("\\bco\\.?\\s*kg\\b"),
            Pattern.compile("\\bug\\s*\\(haftungsbeschraenkt\\)\\b"),
            Pattern.compile("\\bg\\.m\\.b\\.h\\.?\\b"),
            Pattern.compile("\\ba\\.g\\.?\\b"),
            Pattern.compile("\\bk\\.g\\.?\\b"),
            Pattern.compile("\\bo\\.h\\.g\\.?\\b"),
            Pattern.compile("\\bg\\.b\\.r\\.?\\b"),
            Pattern.compile("\\be\\.k\\.?\\b"),
            Pattern.compile("\\be\\.v\\.?\\b"),
            Pattern.compile("\\bs\\.a\\.r\\.l\\.?\\b"),
            Pattern.compile("\\bs\\.a\\.?\\b"),
            Pattern.compile("\\bs\\.p\\.a\\.?\\b"),
            Pattern.compile("\\bb\\.v\\.?\\b"),
            Pattern.compile("\\bn\\.v\\.?\\b"),
            Pattern.compile("\\bs\\.r\\.o\\.?\\b")
    );

    public static String normalizeCompanyName(String input) {
        if (input == null) return null;

        String s = input.trim();
        if (s.isEmpty()) return "";

        s = s.toLowerCase(Locale.ROOT);

        // Umlaute/ß vor Diakritika-Strip
        s = s.replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");

        // Diakritika entfernen (é -> e etc.)
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);
        s = COMBINING_MARKS.matcher(s).replaceAll("");

        // Separatoren zu Space
        s = SEPARATORS_TO_SPACE.matcher(s).replaceAll(" ");

        // EXTRA #2: AND-Connectors vereinheitlichen (alles zu Space)
        s = AND_CONNECTORS.matcher(s).replaceAll(" ");

        // Mehrwort-/Sonder-Schreibweisen der Rechtsformen raus
        for (Pattern p : LEGAL_FORM_PHRASES) {
            s = p.matcher(s).replaceAll(" ");
        }

        // Nur noch [a-z0-9 ] erlauben
        s = NON_ALNUM_SPACE.matcher(s).replaceAll(" ");
        s = MULTI_SPACE.matcher(s).replaceAll(" ").trim();

        if (s.isEmpty()) return "";

        // Token-Filter: Rechtsformen + Org-Noise + 1-Zeichen Tokens entfernen
        String[] parts = s.split(" ");
        StringBuilder out = new StringBuilder(s.length());
        for (String token : parts) {
            if (token != null
                    && token.length() > 1
                    && !LEGAL_FORM_TOKENS.contains(token)
                    && !ORG_NOISE_TOKENS.contains(token)) {

                if (!out.isEmpty()) {
                    out.append(' ');
                }
                out.append(token);
            }
        }

        return out.toString();
    }
}
