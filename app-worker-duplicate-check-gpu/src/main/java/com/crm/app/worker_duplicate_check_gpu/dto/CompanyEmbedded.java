package com.crm.app.worker_duplicate_check_gpu.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompanyEmbedded {
    private String accountName;
    private List<float[]> vectors;

    public CompanyEmbedded(String accountName, List<float[]> vectors) {
        this.accountName = accountName;
        this.vectors = vectors;
    }
}