package com.crm.app.web.statistics;


import com.crm.app.dto.SimpleStats;
import com.crm.app.port.customer.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CustomerRepositoryPort customerRepositoryPort;

    public SimpleStats getSimpleStats() {
        Optional<SimpleStats> simpleStats = customerRepositoryPort.getSimpleStats();
        return simpleStats.orElseGet(SimpleStats::new);
    }

}
