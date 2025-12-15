package com.crm.app.worker_duplicate_check_gpu.dto;

import com.crm.app.dto.DuplicateCheckEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CompanyEmbedded extends DuplicateCheckEntry {

    @Getter
    public static class SimilarCompany {
        EmbeddingMatchType matchType;
        CompanyEmbedded companyEmbedded;

        public SimilarCompany(EmbeddingMatchType matchType, CompanyEmbedded companyEmbedded) {
            this.matchType = matchType;
            this.companyEmbedded = companyEmbedded;
        }
    }

    private String normalisedAccountName;
    private String normalisedAddress;
    private List<float[]> vectorsAccountName;
    private List<float[]> vectorsAddress;
    private boolean matchTypeAccountName = false;
    private boolean matchTypeAddress = false;
    private Map<SimilarCompany, Double> similarCompanies = new HashMap<>();
}