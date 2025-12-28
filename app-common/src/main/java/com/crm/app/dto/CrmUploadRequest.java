package com.crm.app.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@SuppressWarnings({"squid:S6218", "squid:S107"})
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
            byte[] content,
            boolean isTest
    ) {
        super(sourceSystem, crmSystem, crmUrl, crmCustomerId, isTest);
        this.uploadId = uploadId;
        this.customerId = customerId;
        this.apiKey = apiKey;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrmUploadRequest other)) return false;

        // Getter defensiv auswerten (einmal!)
        final var thisSourceSystem = this.getSourceSystem();
        final var otherSourceSystem = other.getSourceSystem();

        final var thisCrmSystem = this.getCrmSystem();
        final var otherCrmSystem = other.getCrmSystem();

        final var thisCrmUrl = this.getCrmUrl();
        final var otherCrmUrl = other.getCrmUrl();

        final var thisCrmCustomerId = this.getCrmCustomerId();
        final var otherCrmCustomerId = other.getCrmCustomerId();

        return uploadId == other.uploadId
                && customerId == other.customerId
                && Objects.equals(thisSourceSystem, otherSourceSystem)
                && Objects.equals(thisCrmSystem, otherCrmSystem)
                && Objects.equals(thisCrmUrl, otherCrmUrl)
                && Objects.equals(thisCrmCustomerId, otherCrmCustomerId)
                && Objects.equals(apiKey, other.apiKey)
                && Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        final var sourceSystem = getSourceSystem();
        final var crmSystem = getCrmSystem();
        final var crmUrl = getCrmUrl();
        final var crmCustomerId = getCrmCustomerId();

        int result = Objects.hash(
                uploadId,
                customerId,
                sourceSystem,
                crmSystem,
                crmUrl,
                crmCustomerId,
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
