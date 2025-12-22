package com.crm.app.e2e.smoke_tests;

import com.crm.app.e2e.AbstractE2eTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
class DatabaseSmokeE2eTest extends AbstractE2eTest {

    @SpringBootApplication
    static class TestApp {
    }

    @Test
    void dbIsReachable() throws Exception {
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("select 1")) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
        }
    }
}