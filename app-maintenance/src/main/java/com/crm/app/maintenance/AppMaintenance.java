package com.crm.app.maintenance;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
import com.crm.app.config.AppParameters;
import com.crm.app.error.MaintenanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.crm")
@RequiredArgsConstructor
@EnableConfigurationProperties(AppDataSourceProperties.class)
public class AppMaintenance implements CommandLineRunner, ExitCodeGenerator {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppMaintenance.class);
        int code = SpringApplication.exit(app.run(args));
        System.exit(code);
    }

    @Override
    public void run(String... args) throws Exception {
        AppParameters appParameters = parseArgs(args);
        log.info("Starte Batch-Prozess…");
        log.info("Upload-Id={}", appParameters.getUploadId());
        log.info("Batch-Prozess beendet.");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    private AppParameters parseArgs(String... args) {
        if (args == null || args.length == 0) {
            throw new MaintenanceException("No arguments were provided [--get-file {id}");
        }
        AppParameters appParameters = new AppParameters();
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("--get-file")) {
                appParameters.setUploadId(Long.parseLong(nextArg(args, i, "---get-file")));
                i += 2;
            } else {
                i++;
            }
        }

        return appParameters;
    }

    private static String nextArg(String[] args, int i, String flag) {
        if (i + 1 >= args.length) {
            throw new IllegalArgumentException(flag + " requires a value");
        }
        return args[i + 1];
    }
}


