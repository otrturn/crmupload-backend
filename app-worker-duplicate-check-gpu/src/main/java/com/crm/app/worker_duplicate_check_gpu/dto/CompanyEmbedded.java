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
    private String normalisedAccountName;
    private List<float[]> vectors;
    private Map<CompanyEmbedded, Double> similarCompanies = new HashMap<>();
}