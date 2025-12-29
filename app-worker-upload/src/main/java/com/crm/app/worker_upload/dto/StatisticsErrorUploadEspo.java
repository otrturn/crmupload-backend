package com.crm.app.worker_upload.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsErrorUploadEspo {
    long nAccountsInCrm = 0;
    long nAccountsReceived = 0;
    long nAccountsMeantToBeAdded = 0;
    long nAccountsAdded = 0;
    long nAccountsRejected = 0;
    long nAccountsIgnored = 0;
    long nContactsInCrm = 0;
    long nContactsReceived = 0;
    long nContactsMeantToBeAdded = 0;
    long nContactsAdded = 0;
    long nContactsRejected = 0;
    long nContactsIgnored = 0;
    long nSecondsForEspoLoad = 0;
    long nSecondsForEspoUpload = 0;
}
