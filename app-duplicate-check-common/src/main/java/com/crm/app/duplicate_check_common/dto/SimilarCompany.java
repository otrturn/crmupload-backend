package com.crm.app.duplicate_check_common.dto;

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
