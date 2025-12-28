package com.crm.app.web;

import com.crm.app.dto.*;
import com.crm.app.web.customer.CustomerProfileService;
import com.crm.app.web.duplicate_check.DuplicateCheckService;
import com.crm.app.web.upload.CrmUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerProfileService customerProfileService;
    private final CrmUploadService crmUploadService;
    private final DuplicateCheckService duplicateCheckService;

    @GetMapping("/me/{emailAddress:.+}")
    public ResponseEntity<CustomerProfile> getMe(@PathVariable("emailAddress") String emailAddress) {
        CustomerProfile response = customerProfileService.getCustomerByEmail(emailAddress);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-customer/{emailAddress:.+}")
    public ResponseEntity<Void> updateCustomer(
            @PathVariable("emailAddress") String emailAddress,
            @RequestBody CustomerProfile request
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

    @GetMapping("/get-duplicate-check-history/{emailAddress:.+}")
    public ResponseEntity<DuplicateCheckHistoryResponse> getDuplicateCheckHistory(@PathVariable("emailAddress") String emailAddress) {
        List<DuplicateCheckHistory> response = duplicateCheckService.getDuplicateCheckHistoryByEmail(emailAddress);
        return ResponseEntity.ok(new DuplicateCheckHistoryResponse(response));
    }

    @GetMapping("/get-status/{emailAddress:.+}")
    public ResponseEntity<CustomerStatusResponse> getStatus(@PathVariable("emailAddress") String emailAddress) {
        CustomerStatusResponse response = customerProfileService.getStatus(emailAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
