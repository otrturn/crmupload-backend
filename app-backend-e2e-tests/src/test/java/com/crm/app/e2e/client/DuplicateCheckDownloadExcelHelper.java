package com.crm.app.e2e.client;

public enum DuplicateCheckDownloadExcelHelper {
    SAMPLE(
            "/api/duplicate-check/downloads/help/excel-sample",
            "dubletten-pruefung-lexware-beispiel.xlsx"
    ),
    SAMPLE_ANSWER(
            "/api/duplicate-check/downloads/help/excel-sample-answer",
            "dubletten-pruefung-lexware-antwort.xlsx"
    );

    private final String path;
    private final String expectedFilename;

    DuplicateCheckDownloadExcelHelper(String path, String expectedFilename) {
        this.path = path;
        this.expectedFilename = expectedFilename;
    }

    public String path() {
        return path;
    }

    public String expectedFilename() {
        return expectedFilename;
    }
}
