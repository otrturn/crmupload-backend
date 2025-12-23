package com.crm.app.e2e.e2e_download_excel_help;

import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.DuplicateCheckDownloadExcelClient;
import com.crm.app.e2e.client.DuplicateCheckDownloadExcelHelper;
import com.crm.app.e2e.client.DuplicateCheckDownloadExcelResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eDuplicateCheckDownloadExcel extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_customerGetStatus() {

        DuplicateCheckDownloadExcelClient duplicateCheckDownloadExcelClient = new DuplicateCheckDownloadExcelClient(e2eProperties);

        DuplicateCheckDownloadExcelResult duplicateCheckDownloadExcelResult;
        DuplicateCheckDownloadExcelResult.Success downloadExcelHelpSuccess;
        String cd;

        /*
        Download Excel file, to be used for upload
         */
        duplicateCheckDownloadExcelResult = duplicateCheckDownloadExcelClient.download(DuplicateCheckDownloadExcelHelper.SAMPLE);
        assertThat(duplicateCheckDownloadExcelResult).isInstanceOf(DuplicateCheckDownloadExcelResult.Success.class);
        downloadExcelHelpSuccess = (DuplicateCheckDownloadExcelResult.Success) duplicateCheckDownloadExcelResult;

        cd = downloadExcelHelpSuccess.contentDisposition();
        assertThat(cd)
                .contains("attachment")
                .contains(DuplicateCheckDownloadExcelHelper.SAMPLE.expectedFilename());

        /*
        Download Excel file, to be used to verify the answer
         */
        duplicateCheckDownloadExcelResult = duplicateCheckDownloadExcelClient.download(DuplicateCheckDownloadExcelHelper.SAMPLE_ANSWER);
        assertThat(duplicateCheckDownloadExcelResult).isInstanceOf(DuplicateCheckDownloadExcelResult.Success.class);
        downloadExcelHelpSuccess = (DuplicateCheckDownloadExcelResult.Success) duplicateCheckDownloadExcelResult;

        cd = downloadExcelHelpSuccess.contentDisposition();
        assertThat(cd)
                .contains("attachment")
                .contains(DuplicateCheckDownloadExcelHelper.SAMPLE_ANSWER.expectedFilename());
    }
}