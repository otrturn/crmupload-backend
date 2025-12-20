package com.crm.app.worker_upload.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsUploadEspo {
    long nAccountsInCrm = 0;
    long nAccountsReceived = 0;
    long nAccountsAdded = 0;
    long nAccountsIgnored = 0;
    long nContactsInCrm = 0;
    long nContactsReceived = 0;
    long nContactsAdded = 0;
    long nContactsIgnored = 0;
}
