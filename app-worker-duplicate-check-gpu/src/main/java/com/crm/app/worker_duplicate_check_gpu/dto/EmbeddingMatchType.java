package com.crm.app.worker_duplicate_check_gpu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbeddingMatchType {
    boolean accountNameMatch;
    AddressMatchCategory addressMatchCategory;

    public EmbeddingMatchType(boolean accountNameMatch, AddressMatchCategory addressMatchCategory) {
        this.accountNameMatch = accountNameMatch;
        this.addressMatchCategory = addressMatchCategory;
    }
}
