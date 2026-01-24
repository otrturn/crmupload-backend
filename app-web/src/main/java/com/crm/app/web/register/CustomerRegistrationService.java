package com.crm.app.web.register;

import com.crm.app.dto.*;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.web.activation.CustomerActivationService;
import com.crm.app.web.config.AppWebActivationProperties;
import com.crm.app.web.error.CustomerAlreadyExistsException;
import com.crm.app.web.error.CustomerProductInvalidException;
import com.crm.app.web.error.CustomerTermsVersionInvalidException;
import com.crm.app.web.validation.RegisterRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.crm.app.util.IdentityNumberCreator.createCustomerNumber;
import static com.crm.app.web.validation.RequestValidator.isNotValidGermanTaxId;

@Service
@RequiredArgsConstructor
public class CustomerRegistrationService {

    private final UserAccountRegistrationService userAccountRegistrationService;
    private final CustomerRepositoryPort customerRepository;
    private final CustomerActivationService customerActivationService;
    private final AppWebActivationProperties appWebActivationProperties;

    @Transactional
    public ResponseEntity<RegisterResponse> registerCustomer(RegisterRequest request, String ipAddress, String userAgent) {
        String emailAddress = request.emailAddress();

        RegisterRequestValidator.assertValid(request);

        /*
        Email Address
         */
        if (customerRepository.emailExists(emailAddress)) {
            throw new CustomerAlreadyExistsException("Customer with email already exists: " + emailAddress);
        }

        /*
        Products
         */
        for (String product : request.products()) {
            if (!AppConstants.availableProducts().contains(product)) {
                String msg = String.format(
                        "Customer with email %s -> unknown product %s",
                        emailAddress, product);
                throw new CustomerProductInvalidException(msg);
            }
        }

        if (request.products().stream().distinct().count() != request.products().size()) {
            throw new CustomerProductInvalidException(
                    String.format(
                            "registration: Customer %s invalid list of products",
                            emailAddress
                    ));
        }

        /*
        Terms of service
         */
        if (!appWebActivationProperties.getAllowedTermsVersions().contains(request.termsVersion())) {
            String msg = String.format(
                    "Customer with email %s -> invalid/unknown terms version %s",
                    emailAddress, request.termsVersion());
            throw new CustomerTermsVersionInvalidException(msg);
        }

        UserAccountRegistrationResult accountResult =
                userAccountRegistrationService.registerUserAccount(emailAddress, request.password());

        long customerId = customerRepository.nextCustomerId();

        Customer customer = new Customer(
                customerId,
                createCustomerNumber(customerId),
                accountResult.userId(),
                request.firstname(),
                request.lastname(),
                request.companyName(),
                request.emailAddress(),
                request.phoneNumber(),
                request.adrline1(),
                request.adrline2(),
                request.postalcode(),
                request.city(),
                request.country(),
                request.taxId(),
                request.vatId(),
                request.products(),
                null
        );

        customerRepository.insertCustomer(customer);

        CustomerAcknowledgement customerAcknowledgement = new CustomerAcknowledgement(customerId,
                request.agbAccepted(),
                request.isEntrepreneur(),
                request.requestImmediateServiceStart(),
                request.acknowledgeWithdrawalLoss(),
                request.termsVersion(),
                ipAddress,
                userAgent
        );

        customerRepository.insertCustomerAcknowledgement(customerAcknowledgement);

        checkForVerificationTasks(customer);

        customerActivationService.sendActivationEmail(
                emailAddress,
                request.firstname() + " " + request.lastname(),
                customerId
        );

        return ResponseEntity
                .status(201)
                .body(new RegisterResponse(accountResult.jwtToken()));
    }

    private void checkForVerificationTasks(Customer customer) {
        if ("DE".equals(customer.country()) && (isNotValidGermanTaxId(customer.taxId()))) {
            customerRepository.insertCustomerVerificationTask(customer.customerId(), new CustomerVerificationTask(customer.customerId(), customerRepository.nextCustomerVerificationTaskId(), "taxId:" + customer.taxId()));
        }
    }

}
