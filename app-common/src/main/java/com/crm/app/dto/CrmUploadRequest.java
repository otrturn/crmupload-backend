package com.crm.app.dto;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Objects;

public record CrmUploadRequest(
        long uploadId,
        long customerId,
        String sourceSystem,
        String crmSystem,
        String crmUrl,
        String crmCustomerId,
        String apiKey,
        byte[] content
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrmUploadRequest other)) return false;
        return uploadId == other.uploadId &&
                customerId == other.customerId &&
                Objects.equals(sourceSystem, other.sourceSystem) &&
                Objects.equals(crmSystem, other.crmSystem) &&
                Objects.equals(crmUrl, other.crmUrl) &&
                Objects.equals(crmCustomerId, other.crmCustomerId) &&
                Objects.equals(apiKey, other.apiKey) &&
                Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                uploadId, customerId, sourceSystem, crmSystem, crmUrl,
                crmCustomerId, apiKey
        );
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    @NonNull
    public String toString() {
        return "CrmUploadRequest[" +
                "uploadId=" + uploadId +
                ", customerId=" + customerId +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", crmSystem='" + crmSystem + '\'' +
                ", crmUrl='" + crmUrl + '\'' +
                ", crmCustomerId='" + crmCustomerId + '\'' +
                ", apiKey='****'" +   // sensible Daten nicht ausgeben 😉
                ", content.length=" + (content == null ? 0 : content.length) +
                ']';
    }
}