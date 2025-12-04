package com.crm.app.tools.BulkUploadBexioToEspo;

import java.nio.file.Paths;

import static com.crm.app.tools.process.UploadConsumerFile.uploadConsumerFile;

@SuppressWarnings("squid:S6437")
public class AppBulkUploadBexioToEspo {
    public static void main(String[] args) {
        uploadConsumerFile(Paths.get("/home/ralf/espocrm-demo/Bexio_Generated.xlsx"), 10, "Bexio", "EspoCRM");
    }

}
