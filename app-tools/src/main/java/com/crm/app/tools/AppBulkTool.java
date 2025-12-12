package com.crm.app.tools;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.tools.process.RegisterCustomers;
import com.crm.app.tools.process.UploadCrmFile;
import com.crm.app.tools.process.UploadDuplicateCheckFile;
import com.crmmacher.bexio.tools.generator.process.BexioGenerateWorkbook;
import com.crmmacher.lexware_excel.tools.generator.process.LexwareGenerateWorkbookDuplicateCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;
import java.util.List;

@Slf4j
@SuppressWarnings("squid:S6437")
@SpringBootApplication(scanBasePackages = "com.crm.app.tools")
@RequiredArgsConstructor
public class AppBulkTool implements CommandLineRunner, ExitCodeGenerator {
    private final RegisterCustomers registerCustomers;
    private final UploadCrmFile uploadCrmFile;
    private final UploadDuplicateCheckFile uploadDuplicateCheckFile;

    private enum TASK {TO_ESPO, TO_DUPLICATE_CHECK}

    private static final String ESPO_CRM_LITERAL = "EspoCRM";

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
                        "Am Dorfplatz 6", null, "57610", "Ingelbach", "DE", "test123",
                        List.of(AppConstants.PRODUCT_CRM_UPLOAD, AppConstants.PRODUCT_DUPLICATE_CHECK));
                registerCustomers.process(10, request);
            }
            case "--BexioToEspo" -> processBexio(TASK.TO_ESPO);
            case "--BexioToDuplicateCheck" -> processBexio(TASK.TO_DUPLICATE_CHECK);

            case "--LexwareToEspo" -> processLexware(TASK.TO_ESPO);
            case "--LexwareToDuplicateCheck" -> processLexware(TASK.TO_DUPLICATE_CHECK);

            case "--MyExcelToEspo" -> processMyExcel(TASK.TO_ESPO);
            case "--MyExcelToDuplicateCheck" -> processMyExcel(TASK.TO_DUPLICATE_CHECK);

            default -> log.error("Unknown command {}", args[0]);
        }
        log.info("Batch-Prozess beendet.");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    private void processBexio(TASK task) throws Exception {
        int nFiles = 1;
        for (int i = 1; i <= nFiles; i++) {
            String filename = String.format("/home/ralf/espocrm-demo/generated/Bexio_Generated_%05d.xlsx", i);
            BexioGenerateWorkbook.createWorkbook(filename, 10, String.format("%04d", i));
            if (task.equals(TASK.TO_ESPO)) {
                uploadCrmFile.process(Paths.get(filename), 1, "Bexio", ESPO_CRM_LITERAL);
            } else if (task.equals(TASK.TO_DUPLICATE_CHECK)) {
                uploadDuplicateCheckFile.process(Paths.get(filename), 1, "Bexio");
            }
        }
    }

    private void processLexware(TASK task) throws Exception {
        int nFiles = 1;
        for (int i = 1; i <= nFiles; i++) {
            String filename = String.format("/home/ralf/espocrm-demo/generated/Lexware_Generated_Duplicate_Check_%05d.xlsx", i);
            LexwareGenerateWorkbookDuplicateCheck.createWorkbook(filename, 10, String.format("LW-%04d", i));
            if (task.equals(TASK.TO_ESPO)) {
                uploadCrmFile.process(Paths.get(filename), 1, "Lexware", ESPO_CRM_LITERAL);
            } else if (task.equals(TASK.TO_DUPLICATE_CHECK)) {
                uploadDuplicateCheckFile.process(Paths.get(filename), 1, "Lexware");
            }
        }
    }

    private void processMyExcel(TASK task) {
        if (task.equals(TASK.TO_ESPO)) {
            uploadCrmFile.process(Paths.get("/home/ralf/espocrm-demo/MyExcelKunden_V001.xlsx"), 1, "MyExcel", ESPO_CRM_LITERAL);
        } else if (task.equals(TASK.TO_DUPLICATE_CHECK)) {
            uploadDuplicateCheckFile.process(Paths.get("/home/ralf/espocrm-demo/MyExcelKunden_V001.xlsx"), 1, "MyExcel");
        }
    }

}
