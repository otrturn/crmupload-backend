package com.crm.app.worker_duplicate_check;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
import com.crm.app.worker_duplicate_check.config.DuplicateCheckProperties;
import com.crmmacher.espo.importer.bexio_excel.config.BexioCtx;
import com.crmmacher.espo.importer.lexware_excel.config.LexwareCtx;
import com.crmmacher.espo.importer.my_excel.config.MyExcelCtx;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.crm")
@EnableConfigurationProperties({AppDataSourceProperties.class, DuplicateCheckProperties.class, BexioCtx.class, LexwareCtx.class, MyExcelCtx.class})
public class AppWorkerDuplicateCheckApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppWorkerDuplicateCheckApplication.class, args);
    }
}
