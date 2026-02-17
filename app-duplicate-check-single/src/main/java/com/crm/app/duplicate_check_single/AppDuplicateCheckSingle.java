package com.crm.app.duplicate_check_single;

import com.crm.app.duplicate_check_single.process.DuplicateCheckSingleProcessFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.crm")
@RequiredArgsConstructor
public class AppDuplicateCheckSingle implements CommandLineRunner, ExitCodeGenerator {

    private int exitCode = 0;
    private final DuplicateCheckSingleProcessFile duplicateCheckSingleProcessFile;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppDuplicateCheckSingle.class);
        int code = SpringApplication.exit(app.run(args));
        System.exit(code);
    }

    @Override
    public void run(String... args) {
        try {
            Instant start = Instant.now();
            log.info("Starting duplicate check single …");
            duplicateCheckSingleProcessFile.processFile();
            log.info("duplicate check single finished.");
            Duration duration = Duration.between(start, Instant.now());
            log.info(String.format("Duration: %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            exitCode = 1;
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

}

