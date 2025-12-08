package com.crm.app.web.register;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.dto.RegisterResponse;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.web.activation.CustomerActivationService;
import com.crm.app.web.validation.RegisterRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerRegistrationService {

    private final UserAccountRegistrationService userAccountRegistrationService;
    private final CustomerRepositoryPort customerRepository;
    private final CustomerActivationService customerActivationService;

    @Transactional
    public ResponseEntity<RegisterResponse> registerCustomer(RegisterRequest request) {
        String emailAddress = request.email_address();

        RegisterRequestValidator.assertValid(request);

        if (customerRepository.emailExists(emailAddress)) {
            throw new IllegalStateException("Customer with email already exists: " + emailAddress);
        }

        UserAccountRegistrationResult accountResult =
                userAccountRegistrationService.registerUserAccount(emailAddress, request.password());

        long customerId = customerRepository.nextCustomerId();

        Customer customer = new Customer(
                customerId,
                accountResult.userId(),
                request.firstname(),
                request.lastname(),
                request.company_name(),
                request.email_address(),
                request.phone_number(),
                request.adrline1(),
                request.adrline2(),
                request.postalcode(),
                request.city(),
                request.country()
        );

        customerRepository.insertCustomer(customer);

        customerActivationService.sendActivationEmail(
                emailAddress,
                request.firstname() + " " + request.lastname(),
                customerId
        );

        return ResponseEntity
                .status(201)
                .body(new RegisterResponse(accountResult.jwtToken()));
    }

}
