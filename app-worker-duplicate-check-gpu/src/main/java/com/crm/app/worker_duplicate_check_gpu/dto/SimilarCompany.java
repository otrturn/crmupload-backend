package com.crm.app.worker_duplicate_check_gpu.dto;

import lombok.Getter;

@Getter
public class SimilarCompany {
    EmbeddingMatchType matchType;
    CompanyEmbedded companyEmbedded;

    public SimilarCompany(EmbeddingMatchType matchType, CompanyEmbedded companyEmbedded) {
        this.matchType = matchType;
        this.companyEmbedded = companyEmbedded;
    }
}
