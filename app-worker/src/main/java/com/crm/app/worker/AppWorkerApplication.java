package com.crm.app.worker;

import com.crm.app.config.AppDataSourceProperties;
import com.crm.app.worker.config.ConsumerUploadProperties;
import com.crmmacher.espo.importer.bexio_excel.config.BexioCtx;
import com.crmmacher.espo.importer.my_excel.config.MyExcelCtx;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.crm")
@EnableConfigurationProperties({AppDataSourceProperties.class, ConsumerUploadProperties.class, BexioCtx.class, MyExcelCtx.class})
public class AppWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppWorkerApplication.class, args);
    }
}
