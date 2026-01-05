package com.crm.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleStats {
    long nCustomer = 0;
    long nCustomerEnabled = 0;
    long nProductsCrmUpload = 0;
    long nProductsDuplicateCheck = 0;
}
