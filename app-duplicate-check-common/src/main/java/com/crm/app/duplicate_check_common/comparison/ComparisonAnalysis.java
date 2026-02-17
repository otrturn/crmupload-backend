package com.crm.app.duplicate_check_common.comparison;

import com.crm.app.duplicate_check_common.config.DuplicateCheckGpuProperties;
import com.crm.app.duplicate_check_common.dto.AddressMatchCategory;
import com.crm.app.duplicate_check_common.dto.CompanyEmbedded;
import com.crm.app.duplicate_check_common.dto.EmbeddingMatchType;
import com.crm.app.duplicate_check_common.dto.SimilarCompany;
import com.crm.app.util.EmbeddingUtils;
import com.crm.app.worker_common.dto.StatisticsDuplicateCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComparisonAnalysis {
    private final DuplicateCheckGpuProperties properties;

    public void comparisonAnalysis(List<CompanyEmbedded> companiesEmbedded) {
        for (int i = 0; i < companiesEmbedded.size(); i++) {
            CompanyEmbedded a = companiesEmbedded.get(i);

            for (int j = i + 1; j < companiesEmbedded.size(); j++) {
                CompanyEmbedded b = companiesEmbedded.get(j);

                if (postalCodeAreaEqual(a, b)) {
                    EmbeddingMatchType embeddingMatchType;
                    if (properties.isPerformAddressAnalysis()) {
                        embeddingMatchType = evaluateMatchWithAddress(a, b);
                    } else {
                        embeddingMatchType = evaluateMatchAccountNameOnly(a, b);
                    }

                    if (entryMatches(embeddingMatchType))
                        a.getSimilarCompanies().add(new SimilarCompany(embeddingMatchType, b));
                }
            }
        }
    }

    private boolean entryMatches(EmbeddingMatchType embeddingMatchType) {
        return embeddingMatchType.isAccountNameMatch()
                || embeddingMatchType.getAddressMatchCategory().equals(AddressMatchCategory.POSSIBLE)
                || embeddingMatchType.getAddressMatchCategory().equals(AddressMatchCategory.MATCH);
    }

    private EmbeddingMatchType evaluateMatchAccountNameOnly(CompanyEmbedded a, CompanyEmbedded b) {
        return cosineAccountName(a, b) < properties.getCosineSimilarityThresholdAccountName() ?
                new EmbeddingMatchType(false, AddressMatchCategory.NO_MATCH) :
                new EmbeddingMatchType(true, AddressMatchCategory.NO_MATCH);
    }

    @SuppressWarnings("squid:S1194")
    private EmbeddingMatchType evaluateMatchWithAddress(CompanyEmbedded a, CompanyEmbedded b) {
        double nameSim = cosineAccountName(a, b);
        boolean nameMatch = nameSim >= properties.getCosineSimilarityThresholdAccountName();

        com.crm.app.duplicate_check_common.matcher.AddressMatcher.AddressKey addressKeyA = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of(a.getStreet(), a.getCity());
        com.crm.app.duplicate_check_common.matcher.AddressMatcher.AddressKey addressKeyB = com.crm.app.duplicate_check_common.matcher.AddressMatcher.of(b.getStreet(), b.getCity());
        com.crm.app.duplicate_check_common.matcher.AddressMatcher.MatchResult addressMatchResult = com.crm.app.duplicate_check_common.matcher.AddressMatcher.match(addressKeyA, addressKeyB);

        return new EmbeddingMatchType(nameMatch, addressMatchResult.category());
    }

    private double cosineAccountName(CompanyEmbedded a, CompanyEmbedded b) {
        return EmbeddingUtils.cosineSim(
                a.getVectorsAccountName().get(0),
                b.getVectorsAccountName().get(0)
        );
    }

    private boolean postalCodeAreaEqual(CompanyEmbedded companyEmbedded1, CompanyEmbedded companyEmbedded2) {
        return companyEmbedded1.getPostalCode().charAt(0) == companyEmbedded2.getPostalCode().charAt(0);
    }

    public StatisticsDuplicateCheck setStatistics(List<CompanyEmbedded> companiesEmbedded, Map<String, List<CompanyEmbedded>> emailDuplicates) {
        com.crm.app.worker_common.dto.StatisticsDuplicateCheck statisticsDuplicateCheck = new com.crm.app.worker_common.dto.StatisticsDuplicateCheck();
        statisticsDuplicateCheck.setNEntries(companiesEmbedded.size());
        long accountNameMatches = 0;
        long nAddressMatchesPossible = 0;
        long nAddressMatchesProbable = 0;

        for (CompanyEmbedded company : companiesEmbedded) {
            for (SimilarCompany similar : company.getSimilarCompanies()) {
                EmbeddingMatchType mt = similar.getMatchType();

                if (mt.isAccountNameMatch()) {
                    accountNameMatches++;
                }

                if (mt.getAddressMatchCategory() == AddressMatchCategory.POSSIBLE) {
                    nAddressMatchesPossible++;
                } else if (mt.getAddressMatchCategory() == AddressMatchCategory.MATCH) {
                    nAddressMatchesProbable++;
                }
            }
        }
        statisticsDuplicateCheck.setNDuplicateAccountNames(accountNameMatches);
        statisticsDuplicateCheck.setNAddressMatchesPossible(nAddressMatchesPossible);
        statisticsDuplicateCheck.setNAddressMatchesProbable(nAddressMatchesProbable);
        statisticsDuplicateCheck.setNEmailMatches(emailDuplicates.size());
        return statisticsDuplicateCheck;
    }

    public Map<String, List<CompanyEmbedded>> emailAnalysis(List<CompanyEmbedded> companiesEmbedded) {
        if (companiesEmbedded == null || companiesEmbedded.isEmpty()) {
            return Map.of();
        }

        Map<String, List<CompanyEmbedded>> grouped = companiesEmbedded.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getEmailAddress() != null)
                .map(c -> new AbstractMap.SimpleEntry<>(normalizeEmail(c.getEmailAddress()), c))
                .filter(e -> !e.getKey().isBlank())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
