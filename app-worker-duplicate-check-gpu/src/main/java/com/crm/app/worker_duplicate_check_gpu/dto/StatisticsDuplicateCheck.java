package com.crm.app.worker_duplicate_check_gpu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsDuplicateCheck {
    long nEntries = 0;
    long nDuplicateAccountNames = 0;
    long nAddressMatchesProbable = 0;
    long nAddressMatchesPossible = 0;
    long nEmailMatches = 0;
}
