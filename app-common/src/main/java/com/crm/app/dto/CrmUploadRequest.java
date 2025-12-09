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
@SuppressWarnings("squid:S6218")
public class CrmUploadRequest extends CrmUploadCoreInfo {

    private long uploadId;
    private long customerId;
    private String apiKey;
    private byte[] content;

    public CrmUploadRequest(
            long uploadId,
            long customerId,
            String sourceSystem,
            String crmSystem,
            String crmUrl,
            String crmCustomerId,
            String apiKey,
            byte[] content
    ) {
        super(sourceSystem, crmSystem, crmUrl, crmCustomerId);
        this.uploadId = uploadId;
        this.customerId = customerId;
        this.apiKey = apiKey;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrmUploadRequest other)) return false;
        return uploadId == other.uploadId &&
                customerId == other.customerId &&
                Objects.equals(getSourceSystem(), other.getSourceSystem()) &&
                Objects.equals(getCrmSystem(), other.getCrmSystem()) &&
                Objects.equals(getCrmUrl(), other.getCrmUrl()) &&
                Objects.equals(getCrmCustomerId(), other.getCrmCustomerId()) &&
                Objects.equals(apiKey, other.apiKey) &&
                Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                uploadId, customerId,
                getSourceSystem(), getCrmSystem(), getCrmUrl(), getCrmCustomerId(),
                apiKey
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
                ", sourceSystem='" + getSourceSystem() + '\'' +
                ", crmSystem='" + getCrmSystem() + '\'' +
                ", crmUrl='" + getCrmUrl() + '\'' +
                ", crmCustomerId='" + getCrmCustomerId() + '\'' +
                ", apiKey='****'" +
                ", content.length=" + (content == null ? 0 : content.length) +
                ']';
    }
}
