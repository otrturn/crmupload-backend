package com.crm.app.web.maintenance;

import com.crm.app.port.maintenance.MaintenancePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceService {

    private final MaintenancePort maintenance;

    public void processPageVisited(String pageId) {
        maintenance.insertPageVisited(pageId);
    }
}
