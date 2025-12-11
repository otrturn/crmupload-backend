package com.crm.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings({"squid:S6218", "squid:S107"})
public class DuplicateCheckRequest {

    private long duplicateCheckId;
    private long customerId;
    private String sourceSystem;
    private byte[] content;

    public DuplicateCheckRequest(
            long duplicateCheckId,
            long customerId,
            String sourceSystem,
            byte[] content
    ) {
        this.duplicateCheckId = duplicateCheckId;
        this.customerId = customerId;
        this.sourceSystem = sourceSystem;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DuplicateCheckRequest other)) return false;
        return duplicateCheckId == other.duplicateCheckId &&
                customerId == other.customerId &&
                Objects.equals(getSourceSystem(), other.getSourceSystem()) &&
                Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                duplicateCheckId, customerId,
                getSourceSystem()
        );
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    @NonNull
    public String toString() {
        return "CrmUploadRequest[" +
                "duplicateCheckId=" + duplicateCheckId +
                ", customerId=" + customerId +
                ", sourceSystem='" + getSourceSystem() + '\'' +
                ", content.length=" + (content == null ? 0 : content.length) +
                ']';
    }
}
