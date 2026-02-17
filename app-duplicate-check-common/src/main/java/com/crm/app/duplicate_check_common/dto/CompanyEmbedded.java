package com.crm.app.duplicate_check_common.dto;

import com.crm.app.dto.DuplicateCheckEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompanyEmbedded extends DuplicateCheckEntry {
    private String normalisedAccountName;
    private List<float[]> vectorsAccountName;
    private List<SimilarCompany> similarCompanies = new ArrayList<>();
}