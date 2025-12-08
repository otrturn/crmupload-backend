package com.crm.app.web;

import com.crm.app.web.maintenance.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PutMapping("/page-visited/{pageId:.+}")
    public ResponseEntity<Void> updateCustomer(
            @PathVariable("pageId") String pageId
    ) {
        maintenanceService.processPageVisited(pageId);
        return ResponseEntity.noContent().build();
    }

}
