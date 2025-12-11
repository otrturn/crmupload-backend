package com.crm.app;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
import com.crm.app.config.AppParameters;
import com.crm.app.error.MaintenanceException;
import com.crm.app.process.GetDuplicateCheck;
import com.crm.app.process.GetUploadFile;
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

    private final GetUploadFile getUploadFile;
    private final GetDuplicateCheck getDuplicateCheck;
    private Option option = null;


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppMaintenance.class);
        int code = SpringApplication.exit(app.run(args));
        System.exit(code);
    }

    @Override
    public void run(String... args) throws Exception {
        AppParameters appParameters = parseArgs(args);
        log.info("Starte Batch-Prozess…");
        if (option == null) {
            throw new MaintenanceException("No valid arguments were provided [--get-upload-file {id}");
        } else if (option.equals(Option.GET_UPLOAD_FILE)) {
            getUploadFile.get(appParameters.getUploadId());
        }else if (option.equals(Option.GET_DUPLICATE_CHECK_FILE)) {
            getDuplicateCheck.get(appParameters.getDuplicateCheckId());
        }
        log.info("Batch-Prozess beendet.");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    private AppParameters parseArgs(String... args) {
        if (args == null || args.length == 0) {
            throw new MaintenanceException("No arguments were provided [--get-upload-file {id}");
        }
        AppParameters appParameters = new AppParameters();
        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "--get-upload-file" -> {
                    appParameters.setUploadId(Long.parseLong(nextArg(args, i, "---get-file")));
                    option = Option.GET_UPLOAD_FILE;
                    i += 2;
                }
                case "--get-duplicate-check-file" -> {
                    appParameters.setDuplicateCheckId(Long.parseLong(nextArg(args, i, "---get-file")));
                    option = Option.GET_DUPLICATE_CHECK_FILE;
                    i += 2;
                }
                default -> i++;
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

    private enum Option {
        GET_UPLOAD_FILE, GET_DUPLICATE_CHECK_FILE
    }

}