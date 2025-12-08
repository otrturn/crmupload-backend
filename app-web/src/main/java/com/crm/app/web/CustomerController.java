package com.crm.app.web;

import com.crm.app.dto.*;
import com.crm.app.web.customer.CustomerProfileService;
import com.crm.app.web.error.CustomerNotFoundException;
import com.crm.app.web.upload.CrmUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerProfileService customerProfileService;
    private final CrmUploadService crmUploadService;

    @GetMapping("/me/{emailAddress:.+}")
    public ResponseEntity<CustomerProfileResponse> getMe(@PathVariable("emailAddress") String emailAddress) {
        CustomerProfileResponse response = customerProfileService.getCustomerByEmail(emailAddress);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-customer/{emailAddress:.+}")
    public ResponseEntity<Void> updateCustomer(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody CustomerProfileRequest request
    ) {
        customerProfileService.updateCustomerProfile(emailAddress, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-password/{emailAddress:.+}")
    public ResponseEntity<Void> updatePassword(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody UpdatePasswordRequest request
    ) {
        customerProfileService.updateCustomerPassword(emailAddress, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-upload-history/{emailAddress:.+}")
    public ResponseEntity<CrmUploadHistoryResponse> getCrmUploadHistory(@PathVariable("emailAddress") String emailAddress) {
        List<CrmUploadHistory> response = crmUploadService.getCrmUploadHistoryByEmail(emailAddress);
        return ResponseEntity.ok(new CrmUploadHistoryResponse(response));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Void> handleCustomerNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
