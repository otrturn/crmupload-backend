package com.crm.app.web;

import com.crm.app.web.statistics.StatisticsService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    @GetMapping("/stats")
    public String stats() {
        return GSON.toJson(statisticsService.getSimpleStats());
    }

}
