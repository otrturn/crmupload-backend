package com.crm.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings({"squid:S6218", "squid:S107"})
public class DuplicateCheckRequest {
    private long duplicateCheckId;
    private long customerId;
    private String sourceSystem;
    private byte[] content;

    public DuplicateCheckRequest(long duplicateCheckId, long customerId, String sourceSystem, byte[] content) {
        this.duplicateCheckId = duplicateCheckId;
        this.customerId = customerId;
        this.sourceSystem = sourceSystem;
        this.content = content;
    }
}
