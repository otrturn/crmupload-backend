package com.crm.app.web.customer;


import com.crm.app.dto.CustomerProfileRequest;
import com.crm.app.dto.CustomerProfileResponse;
import com.crm.app.dto.UpdatePasswordRequest;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.web.error.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public CustomerProfileResponse getCustomerByEmail(String emailAddress) {
        CustomerProfileResponse response = customerRepositoryPort.getCustomer(emailAddress);
        if (response == null) {
            throw new CustomerNotFoundException(emailAddress);
        }
        return response;
    }

    public void updateCustomerProfile(String email, CustomerProfileRequest request) {
        int rows = customerRepositoryPort.updateCustomerProfile(email, request);
        if (rows == 0) {
            throw new CustomerNotFoundException(request.email_address());
        }
    }

    public void updateCustomerPassword(String emailAddress, UpdatePasswordRequest request) {

        String hash = passwordEncoder.encode(request.password());
        UpdatePasswordRequest passwordHash = new UpdatePasswordRequest(hash);
        int rows = customerRepositoryPort.updateCustomerPassword(emailAddress, passwordHash);

        if (rows == 0) {
            throw new CustomerNotFoundException(emailAddress);
        }
    }
}
