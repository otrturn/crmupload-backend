package com.crm.app.worker_duplicate_check_gpu.dto;

import com.crm.app.dto.DuplicateCheckEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompanyEmbedded extends DuplicateCheckEntry {
    private List<float[]> vectors;


    public CompanyEmbedded(String accountName, List<float[]> vectors) {
        super();
        this.setAccountName(accountName);
        this.setVectors(vectors);
    }

}