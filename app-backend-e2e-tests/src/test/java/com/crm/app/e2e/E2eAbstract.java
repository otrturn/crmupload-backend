package com.crm.app.e2e;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
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
@SpringBootTest(classes = E2eTestApplication.class)
public abstract class E2eAbstract {

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

    public RegisterRequest baseRegisterRequest() {
        return new RegisterRequest(
                "Jürgen", "Becker", null,
                "ralf+" + System.currentTimeMillis() + "@test.de",
                "01702934959",
                "Teichgarten 17", null,
                "60333", "Frankfurt", "DE",
                "013/815/08153","DE123456789",
                "test1234",
                java.util.List.of(
                        AppConstants.PRODUCT_CRM_UPLOAD,
                        AppConstants.PRODUCT_DUPLICATE_CHECK
                ),
                true, true, true, true,
                "21.12.2025"
        );
    }
}