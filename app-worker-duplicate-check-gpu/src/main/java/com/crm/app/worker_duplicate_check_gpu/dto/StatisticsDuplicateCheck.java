package com.crm.app.worker_duplicate_check_gpu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsDuplicateCheck {
    long nEntries = 0;
    long nDuplicateAccountNames = 0;
    long nAddressesMatch = 0;
    long nAddressesPossible = 0;
}
