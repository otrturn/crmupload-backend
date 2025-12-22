package com.crm.app.e2e;

import com.crm.app.e2e.support.DbTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

@ActiveProfiles("e2e")
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest(classes = TestE2eApplication.class)
public abstract class AbstractE2eTest {

    @TestConfiguration
    static class E2eDataSourceTestConfig {
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource dataSource() {
            return DataSourceBuilder.create().build();
        }
    }

    @Autowired
    public DataSource dataSource;

    @BeforeEach
    void beforeEach() {
        DbTestSupport.prepareDatabase(dataSource);
    }

}