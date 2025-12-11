package com.crm.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class DuplicateCheckHistory {

    private String sourceSystem;
    private Timestamp ts;
    private String status;

    public DuplicateCheckHistory(
            Timestamp ts,
            String sourceSystem,
            String status
    ) {
        this.sourceSystem = sourceSystem;
        this.ts = ts;
        this.status = status;
    }
}