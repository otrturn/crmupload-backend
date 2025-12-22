package com.crm.app.e2e;

import com.crm.app.e2e.support.DbTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DatabaseSmokeE2eTest.TestApp.class)
@ActiveProfiles("e2e")
class DatabaseSmokeE2eTest {

    @SpringBootApplication
    static class TestApp {
    }

    @TestConfiguration
    static class E2eDataSourceTestConfig {
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource dataSource() {
            return DataSourceBuilder.create().build();
        }
    }

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void beforeEach() {
        DbTestSupport.prepareDatabase(dataSource);
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