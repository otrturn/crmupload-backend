package com.crm.app.tools;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.tools.config.AppToolsConfig;
import com.crm.app.tools.process.RegisterCustomers;
import com.crm.app.tools.process.UploadCrmFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@Slf4j
@SuppressWarnings("squid:S6437")
@SpringBootApplication(scanBasePackages = "com.crm.app.tools")
@RequiredArgsConstructor
public class AppBulkTool implements CommandLineRunner, ExitCodeGenerator {
    private final AppToolsConfig appToolsConfig;
    private final RegisterCustomers registerCustomers;
    private final UploadCrmFile uploadCrmFile;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppBulkTool.class);
        int code = SpringApplication.exit(app.run(args));
        System.exit(code);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length != 1) {
            log.error("args.length is {}", args.length);
            return;
        }
        log.info("Starte Batch-Prozess…");
        switch (args[0]) {
            case "--registerCustomer" -> {
                RegisterRequest request = new RegisterRequest("Ralf", "Scholler", null, "ralf@test.de", "01702934959",
                        "Am Dorfplatz 6", null, "57610", "Ingelbach", "DE", "test123");
                registerCustomers.process(10, request);
            }
            case "--BexioToEspo" ->
                    uploadCrmFile.process(Paths.get("/home/ralf/espocrm-demo/Bexio_Generated.xlsx"), 10, "Bexio", "EspoCRM");
            case "--MyExcelToEspo" ->
                    uploadCrmFile.process(Paths.get("/home/ralf/espocrm-demo/MyExcelKunden_V001.xlsx"), 10, "MyExcel", "EspoCRM");
            default -> log.error("Unknown command {}", args[0]);
        }
        log.info("Batch-Prozess beendet.");
    }

    @Override
    public int getExitCode() {
        return 0;
    }
}
