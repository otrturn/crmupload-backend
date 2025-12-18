package com.crm.app.billing;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
import com.crm.app.billing.proccess.GenerateInvoices;
import com.crm.app.billing.proccess.MailInvoices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.crm")
@EnableConfigurationProperties({AppDataSourceProperties.class})
@RequiredArgsConstructor
public class AppBillingApplication implements CommandLineRunner, ExitCodeGenerator {

    private final GenerateInvoices generateInvoices;
    private final MailInvoices mailInvoices;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppBillingApplication.class);
        int code = SpringApplication.exit(app.run(args));
        System.exit(code);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length != 1) {
            log.error(String.format("args.length is %d", args.length));
            return;
        }
        log.info("Starte Batch-Prozess…");
        switch (args[0]) {
            case "--generateInvoices" -> generateInvoices.generateInvoices();

            case "--mailInvoices" -> mailInvoices.mailInvoices();

            default -> log.error(String.format("Unknown command %s", args[0]));
        }
        log.info("Batch-Prozess beendet.");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

}

