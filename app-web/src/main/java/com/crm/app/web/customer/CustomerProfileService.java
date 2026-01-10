package com.crm.app.web.customer;


import com.crm.app.dto.CustomerProfile;
import com.crm.app.dto.CustomerStatusResponse;
import com.crm.app.dto.UpdatePasswordRequest;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.web.error.CustomerBlockedException;
import com.crm.app.web.error.CustomerNotEnabledException;
import com.crm.app.web.error.CustomerNotFoundException;
import com.crm.app.web.error.UpdateRequestInvalidCustomerDataException;
import com.crm.app.web.validation.UpdateRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public CustomerProfile getCustomerByEmail(String emailAddress) {
        if (!customerRepositoryPort.isEnabledByEmail(emailAddress)) {
            throw new CustomerNotEnabledException("Customer with email is not enabled: " + emailAddress);
        }
        if (customerRepositoryPort.isBlockedByEmail(emailAddress)) {
            throw new CustomerBlockedException("Customer with email is blocked: " + emailAddress);
        }
        CustomerProfile response = customerRepositoryPort.getCustomer(emailAddress);
        if (response == null) {
            throw new CustomerNotFoundException(emailAddress);
        }
        return response;
    }

    public void updateCustomerProfile(String emailAddress, CustomerProfile request) {
        UpdateRequestValidator.assertValid(request);
        if (!customerRepositoryPort.isEnabledByEmail(emailAddress)) {
            throw new CustomerNotEnabledException("Customer with email is not enabled: " + emailAddress);
        }
        if (customerRepositoryPort.isBlockedByEmail(emailAddress)) {
            throw new CustomerBlockedException("Customer with email is blocked: " + emailAddress);
        }
        /*
        Email Address
         */
        if (!customerRepositoryPort.emailExists(emailAddress)) {
            throw new CustomerNotFoundException("Customer with email does not exists: " + emailAddress);
        }
        int rows = customerRepositoryPort.updateCustomerProfile(emailAddress, request);
        if (rows == 0) {
            throw new CustomerNotFoundException(emailAddress);
        }
    }

    public void updateCustomerPassword(String emailAddress, UpdatePasswordRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new UpdateRequestInvalidCustomerDataException(emailAddress);
        }
        if (!customerRepositoryPort.isEnabledByEmail(emailAddress)) {
            throw new CustomerNotEnabledException("Customer with email is not enabled: " + emailAddress);
        }
        if (customerRepositoryPort.isBlockedByEmail(emailAddress)) {
            throw new CustomerBlockedException("Customer with email is blocked: " + emailAddress);
        }
        String hash = passwordEncoder.encode(request.password());
        UpdatePasswordRequest passwordHash = new UpdatePasswordRequest(hash);
        int rows = customerRepositoryPort.updateCustomerPassword(emailAddress, passwordHash);

        if (rows == 0) {
            throw new CustomerNotFoundException(emailAddress);
        }
    }

    public CustomerStatusResponse getStatus(String emailAddress) {
        if (!customerRepositoryPort.isEnabledByEmail(emailAddress)) {
            throw new CustomerNotEnabledException("Customer with email is not enabled: " + emailAddress);
        }
        if (customerRepositoryPort.isBlockedByEmail(emailAddress)) {
            throw new CustomerBlockedException("Customer with email is blocked: " + emailAddress);
        }
        boolean isEnabled = customerRepositoryPort.isEnabledByEmail(emailAddress);
        boolean hasOpenCrmUploads = customerRepositoryPort.isHasOpenCrmUploadsByEmail(emailAddress);
        boolean hasOpenDuplicateChecks = customerRepositoryPort.isHasOpenDuplicateChecksByEmail(emailAddress);

        return new CustomerStatusResponse(isEnabled, hasOpenCrmUploads, hasOpenDuplicateChecks);
    }
}
