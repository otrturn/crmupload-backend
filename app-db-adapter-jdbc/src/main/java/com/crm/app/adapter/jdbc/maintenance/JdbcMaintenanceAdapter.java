package com.crm.app.adapter.jdbc.maintenance;

import com.crm.app.port.maintenance.MaintenancePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcMaintenanceAdapter implements MaintenancePort {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void insertPageVisited(String pageId) {
        String sql = """
                INSERT INTO app.page_visits (page_id, visited)
                VALUES (:pageId, now())
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("pageId", pageId);

        jdbc.update(sql, params);
    }

}
