package com.crm.app.tools;

import java.nio.file.Paths;

import static com.crm.app.tools.process.UploadConsumerFile.uploadConsumerFile;

@SuppressWarnings("squid:S6437")
public class AppBulkUploadMyExcelToEspo {
    public static void main(String[] args) {
        uploadConsumerFile(Paths.get("/home/ralf/espocrm-demo/MyExcelKunden_V001.xlsx"), 10, "MyExcel", "EspoCRM");
    }

}
