package com.crm.app.port.customer;

import com.crm.app.dto.*;

import java.util.Optional;

public interface BillingRepositoryPort {

    Optional<CustomerBillingData> getCustomerActiveProductsByCustomerId(long customerId);

}