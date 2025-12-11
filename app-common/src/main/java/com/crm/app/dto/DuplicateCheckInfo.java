package com.crm.app.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
public class DuplicateCheckInfo {

    private long duplicateCheckId;
    private long customerId;
    private String sourceSystem;
    private byte[] content;
    private String status;

    public DuplicateCheckInfo(
            long duplicateCheckId,
            long customerId,
            String sourceSystem,
            byte[] content,
            String status
    ) {
        this.duplicateCheckId = duplicateCheckId;
        this.customerId = customerId;
        this.sourceSystem = sourceSystem;
        this.content = content;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DuplicateCheckInfo other)) return false;
        return duplicateCheckId == other.duplicateCheckId &&
                customerId == other.customerId &&
                Objects.equals(getSourceSystem(), other.getSourceSystem()) &&
                Arrays.equals(content, other.content) &&
                Objects.equals(getStatus(), other.getStatus());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                duplicateCheckId, customerId,
                getSourceSystem(), getStatus()
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
                ", status='" + getStatus() + '\'' +
                ", content.length=" + (content == null ? 0 : content.length) +
                ']';
    }
}
