package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.worker_duplicate_check_gpu.dto.AddressMatchCategory;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AddressMatcher {

    public record AddressKey(
            String streetNorm,
            String cityNorm,
            String houseCore,
            String houseSuffix,
            String houseRangeEnd
    ) {
    }

    public record MatchResult(
            AddressMatchCategory category,
            double score,
            double citySim,
            double streetSim,
            double houseSim
    ) {
    }

    /**
     * Tuning/Schwellenwerte.
     * Alle Werte 0..1
     */
    public record MatchConfig(
            double cityGate,              // wenn kleiner -> sofort NO_MATCH
            double matchThreshold,        // ab hier MATCH
            double possibleThreshold,     // ab hier POSSIBLE, darunter NO_MATCH
            double strongStreetIfHouseMissing,
            // wenn nur eine Hausnummer vorhanden: MATCH nur, wenn streetSim >= dieser Wert
            double wHouse,
            double wStreet,
            double wCity
    ) {
        public MatchConfig {
            // simple guardrails
            if (wHouse + wStreet + wCity < 0.999 || wHouse + wStreet + wCity > 1.001) {
                throw new IllegalArgumentException("Weights must sum to 1.0");
            }
        }

        public static MatchConfig tunedGermanDefaults() {
            return new MatchConfig(
                    0.90, // cityGate: toleranter bei Ortsteil/Abkürzung
                    0.88, // MATCH ab 0.88
                    0.80, // POSSIBLE ab 0.80
                    0.93, // wenn Hausnummer fehlt (einseitig): nur MATCH bei sehr starker Straße
                    0.45, 0.35, 0.20 // Gewichte wie vorher (anti-FP)
            );
        }
    }

    // ---- parsing ----

    private static final Pattern HOUSE_NO =
            Pattern.compile("\\b(\\d{1,5})(?:\\s*([a-z]))?(?:\\s*[-/]\\s*(\\d{1,5}))?\\b");

    private static final Set<String> STREET_STOPWORDS = Set.of(
            "am", "an", "der", "die", "das", "im", "in", "bei", "zum", "zur", "auf",
            "unter", "ober", "obere", "untere", "hinter", "vor", "vorm", "neben"
    );

    private AddressMatcher() {
    }

    public static AddressKey of(String streetAndHouseNo, String city) {
        String line = normalizeStreetLine(streetAndHouseNo);
        line = normalizeStreetTokens(line);

        String houseCore = "";
        String houseSuffix = "";
        String houseRangeEnd = "";

        Matcher m = HOUSE_NO.matcher(line);
        if (m.find()) {
            houseCore = stripLeadingZeros(m.group(1));
            houseSuffix = (m.group(2) != null) ? m.group(2) : "";
            houseRangeEnd = (m.group(3) != null) ? stripLeadingZeros(m.group(3)) : "";

            line = (line.substring(0, m.start()) + " " + line.substring(m.end())).trim();
            line = line.replaceAll("\\s+", " ");
        }

        String streetNorm = normalizeStreetName(line);
        String cityNorm = normalizeCityName(city);

        return new AddressKey(streetNorm, cityNorm, houseCore, houseSuffix, houseRangeEnd);
    }

    // ---- matching (default tuned) ----

    public static MatchResult match(AddressKey a, AddressKey b) {
        return match(a, b, MatchConfig.tunedGermanDefaults());
    }

    public static MatchResult match(AddressKey a, AddressKey b, MatchConfig cfg) {

        // 1) City similarity (Gatekeeper)
        double citySim = bestOf(
                jaroWinkler(a.cityNorm(), b.cityNorm()),
                levenshteinRatio(a.cityNorm(), b.cityNorm()),
                cityTokenOverlap(a.cityNorm(), b.cityNorm()) // neu: robust bei "a. m." / "v d"
        );

        if (citySim < cfg.cityGate()) {
            return new MatchResult(AddressMatchCategory.NO_MATCH, 0.0, clamp01(citySim), 0.0, 0.0);
        }

        // 2) House similarity (harter Anker)
        double houseSim = houseSimilarity(a, b);

        boolean aHasHouse = !isBlank(a.houseCore());
        boolean bHasHouse = !isBlank(b.houseCore());
        if (aHasHouse && bHasHouse && houseSim == 0.0) {
            return new MatchResult(AddressMatchCategory.NO_MATCH, 0.0, clamp01(citySim), 0.0, 0.0);
        }

        // 3) Street similarity
        double streetSim = streetSimilarity(a.streetNorm(), b.streetNorm());

        // 4) Final score
        double score = cfg.wHouse() * houseSim + cfg.wStreet() * streetSim + cfg.wCity() * citySim;
        score = clamp01(score);

        // 5) Kategorie
        AddressMatchCategory cat;
        if (score >= cfg.matchThreshold()) cat = AddressMatchCategory.MATCH;
        else if (score >= cfg.possibleThreshold()) cat = AddressMatchCategory.POSSIBLE;
        else cat = AddressMatchCategory.NO_MATCH;

        // 6) Extra-Schutz: einseitig fehlende Hausnummer => MATCH nur bei sehr starker Straße
        if (cat == AddressMatchCategory.MATCH && (aHasHouse ^ bHasHouse) && streetSim < cfg.strongStreetIfHouseMissing()) {
            cat = AddressMatchCategory.POSSIBLE;
        }

        return new MatchResult(cat, score, clamp01(citySim), clamp01(streetSim), clamp01(houseSim));
    }

    // ---- similarity parts ----

    private static double houseSimilarity(AddressKey a, AddressKey b) {
        boolean aHas = !isBlank(a.houseCore());
        boolean bHas = !isBlank(b.houseCore());

        if (!aHas && !bHas) return 0.60; // beide ohne -> möglich, aber unsicher
        if (aHas ^ bHas) return 0.40;     // eine fehlt -> unsicher

        String ac = a.houseCore();
        String bc = b.houseCore();

        if (!ac.equals(bc)) {
            if (containsRange(a, bc) || containsRange(b, ac)) return 0.75;
            return 0.0;
        }

        String as = nullToEmpty(a.houseSuffix());
        String bs = nullToEmpty(b.houseSuffix());

        if (as.equals(bs)) return 1.0;
        if (isBlank(as) || isBlank(bs)) return 0.70;
        return 0.60;
    }

    private static boolean containsRange(AddressKey key, String otherCore) {
        if (isBlank(key.houseCore()) || isBlank(key.houseRangeEnd()) || isBlank(otherCore)) return false;
        try {
            int start = Integer.parseInt(key.houseCore());
            int end = Integer.parseInt(key.houseRangeEnd());
            int other = Integer.parseInt(otherCore);
            int lo = Math.min(start, end);
            int hi = Math.max(start, end);
            return other >= lo && other <= hi;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static double streetSimilarity(String a, String b) {
        if (isBlank(a) && isBlank(b)) return 1.0;
        if (isBlank(a) || isBlank(b)) return 0.0;

        double jw = jaroWinkler(a, b);
        double lev = levenshteinRatio(a, b);
        double jac = tokenJaccard(a, b);

        return bestOf(jw, lev, jac);
    }

    // ---- normalization ----

    private static String normalizeStreetTokens(String s) {
        if (s == null) return "";
        return s
                .replaceAll("\\bstraße\\b", "str")
                .replaceAll("\\bstr\\.?\\b", "str")
                .replaceAll("\\bstrasse\\b", "str")
                .replaceAll("\\bnr\\.?\\b", " ");
    }

    private static String normalizeStreetName(String s) {
        if (s == null) return "";
        String t = " " + s.trim() + " ";
        for (String sw : STREET_STOPWORDS) {
            t = t.replaceAll("\\b" + Pattern.quote(sw) + "\\b", " ");
        }
        return t.replaceAll("\\s+", " ").trim();
    }

    private static String normalizeCityName(String city) {
        String c = normalizeText(city);

        String[] parts = c.split("\\s+");
        if (parts.length == 0) return "";

        List<String> out = new ArrayList<>(parts.length);

        int i = 0;
        while (i < parts.length) {
            String t = parts[i];
            String next = (i + 1 < parts.length) ? parts[i + 1] : null;

            boolean merged = false;

            if ("a".equals(t) && "m".equals(next)) {
                out.add("am");
                i += 2;
                merged = true;
            } else if ("v".equals(t) && "d".equals(next)) {
                out.add("vd");
                i += 2;
                merged = true;
            }

            if (!merged) {
                out.add(t);
                i += 1;
            }
        }

        return String.join(" ", out).trim();
    }

    static String normalizeText(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.GERMAN);
        t = Normalizer.normalize(t, Normalizer.Form.NFKC);

        t = t.replace("ß", "ss")
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue");

        t = t.replaceAll("[,.;:()\\[\\]{}|\\\\]+", " ");
        t = t.replaceAll("-+", " ");
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }

    private static String stripLeadingZeros(String n) {
        if (n == null) return "";
        return n.replaceFirst("^0+(?!$)", "");
    }

    // ---- city token overlap (robust add-on) ----

    private static double cityTokenOverlap(String a, String b) {
        Set<String> ta = tokens(a);
        Set<String> tb = tokens(b);
        if (ta.isEmpty() && tb.isEmpty()) return 1.0;
        if (ta.isEmpty() || tb.isEmpty()) return 0.0;

        int inter = 0;
        for (String x : ta) if (tb.contains(x)) inter++;
        int min = Math.min(ta.size(), tb.size());
        return (double) inter / (double) min; // overlap wrt smaller set
    }

    // ---- token jaccard ----

    private static double tokenJaccard(String a, String b) {
        Set<String> ta = tokens(a);
        Set<String> tb = tokens(b);
        if (ta.isEmpty() && tb.isEmpty()) return 1.0;
        if (ta.isEmpty() || tb.isEmpty()) return 0.0;

        int inter = 0;
        for (String x : ta) if (tb.contains(x)) inter++;
        int union = ta.size() + tb.size() - inter;
        return union == 0 ? 0.0 : ((double) inter / (double) union);
    }

    private static Set<String> tokens(String s) {
        if (s == null) return Collections.emptySet();

        String[] parts = s.split("\\s+");
        Set<String> out = new HashSet<>();

        for (String p : parts) {
            String t = p.trim();

            boolean keep = !t.isEmpty()
                    && (t.length() > 1 || t.chars().allMatch(Character::isDigit));

            if (keep) {
                out.add(t);
            }
        }
        return out;
    }


    // ---- levenshtein ratio ----

    private static double levenshteinRatio(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        if (a.equals(b)) return 1.0;
        int max = Math.max(a.length(), b.length());
        if (max == 0) return 1.0;
        int d = levenshteinDistance(a, b);
        return 1.0 - ((double) d / (double) max);
    }

    private static int levenshteinDistance(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[m];
    }

    // ---- jaro winkler ----

    private static double jaroWinkler(String s1, String s2) {
        String a = nullToEmpty(s1);
        String b = nullToEmpty(s2);

        if (a.equals(b)) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        MatchData md = computeMatchData(a, b);
        if (md.matches == 0) return 0.0;

        double jaro = jaro(a.length(), b.length(), md.matches, md.transpositions);
        int prefix = commonPrefixLength(a, b, 4);

        return winkler(jaro, prefix, 0.1);
    }

    private static MatchData computeMatchData(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int matchDistance = Math.max(len1, len2) / 2 - 1;
        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = countMatches(s1, s2, matchDistance, s1Matches, s2Matches);
        int transpositions = (matches == 0) ? 0 : countTranspositions(s1, s2, s1Matches, s2Matches);

        return new MatchData(matches, transpositions);
    }

    private static int countMatches(
            String s1,
            String s2,
            int matchDistance,
            boolean[] s1Matches,
            boolean[] s2Matches
    ) {
        int len1 = s1.length();
        int len2 = s2.length();
        int matches = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            int j = findMatchIndex(s1.charAt(i), s2, start, end, s2Matches);
            if (j >= 0) {
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
            }
        }
        return matches;
    }

    private static int findMatchIndex(char c, String s2, int start, int end, boolean[] s2Matches) {
        for (int j = start; j < end; j++) {
            if (!s2Matches[j] && s2.charAt(j) == c) {
                return j;
            }
        }
        return -1;
    }

    private static int countTranspositions(String s1, String s2, boolean[] s1Matches, boolean[] s2Matches) {
        int transpositions = 0;

        int i = nextMatchedIndex(s1Matches, 0);
        int k = nextMatchedIndex(s2Matches, 0);

        while (i < s1.length() && k < s2.length()) {
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }

            i = nextMatchedIndex(s1Matches, i + 1);
            k = nextMatchedIndex(s2Matches, k + 1);
        }

        return transpositions;
    }

    private static int nextMatchedIndex(boolean[] matches, int from) {
        int k = from;
        while (k < matches.length && !matches[k]) {
            k++;
        }
        return k;
    }

    private static double jaro(int len1, int len2, int matches, int transpositions) {
        double t = transpositions / 2.0;
        return (
                (double) matches / len1
                        + (double) matches / len2
                        + (matches - t) / matches
        ) / 3.0;
    }

    private static int commonPrefixLength(String a, String b, int limit) {
        int max = Math.min(limit, Math.min(a.length(), b.length()));
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    private static double winkler(double jaro, int prefixLen, double scaling) {
        return jaro + prefixLen * scaling * (1.0 - jaro);
    }

    private record MatchData(int matches, int transpositions) {
    }

    // ---- utils ----

    private static double bestOf(double... vals) {
        double max = 0.0;
        for (double v : vals) if (v > max) max = v;
        return max;
    }

    private static double clamp01(double v) {
        if (v < 0.0) return 0.0;
        return Math.min(v, 1.0);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String normalizeStreetLine(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.GERMAN);
        t = Normalizer.normalize(t, Normalizer.Form.NFKC);

        t = t.replace("ß", "ss")
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue");

        // Satzzeichen/Trenner -> Space (Bindestrich noch nicht anfassen)
        t = t.replaceAll("[,.;:()\\[\\]{}|\\\\]+", " ");

        // 1) Range zwischen Ziffern stabilisieren: "12 - 14" -> "12-14"
        t = t.replaceAll("(?<=\\d)\\s*[-/]\\s*(?=\\d)", "-");

        // 2) alle anderen Bindestriche (z.B. "koeln-porz" oder "haupt-str") -> Space
        t = t.replaceAll("-(?!\\d)", " ");     // '-' NICHT direkt vor einer Ziffer
        t = t.replaceAll("(?<!\\d)-", " ");    // '-' NICHT direkt nach einer Ziffer

        return t.replaceAll("\\s+", " ").trim();
    }

}
