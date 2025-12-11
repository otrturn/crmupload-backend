package com.crm.app.worker_upload.process;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.worker_upload.config.CrmUploadProperties;
import com.crm.app.worker_upload.util.WorkerUtils;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoAccount;
import com.crmmacher.espo.dto.EspoContact;
import com.crmmacher.espo.dto.EspoEntityPool;
import com.crmmacher.espo.dto.EspoLead;
import com.crmmacher.espo.importer.my_excel.config.MyExcelCtx;
import com.crmmacher.espo.importer.my_excel.util.MyExcelToEspoAccountMapper;
import com.crmmacher.espo.importer.my_excel.util.MyExcelToEspoContactMapper;
import com.crmmacher.espo.importer.my_excel.util.MyExcelToEspoLeadMapper;
import com.crmmacher.espo.importer.my_excel.util.VerifyMyExcelForEspo;
import com.crmmacher.my_excel.dto.MyExcelAccount;
import com.crmmacher.my_excel.dto.MyExcelContact;
import com.crmmacher.my_excel.dto.MyExcelLead;
import com.crmmacher.my_excel.reader.MyExcelReadAccounts;
import com.crmmacher.my_excel.reader.MyExcelReadContacts;
import com.crmmacher.my_excel.reader.MyExcelReadLeads;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.crm.app.worker_upload.util.WorkerUtils.writeExcelToFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadWorkerProcessForMyExcel {

    private final CrmUploadRepositoryPort repository;
    private final CrmUploadProperties properties;
    private final UploadHandlingForEspo uploadHandlingForEspo;
    private final CustomerRepositoryPort customerRepositoryPort;

    private final MyExcelCtx myExcelCtx;

    public void processUploadForEspo(CrmUploadContent upload) {
        Path excelSourcefile = Paths.get(String.format("%s/Upload_MyExcel_%06d.xlsx", properties.getWorkdir(), upload.getUploadId()));
        Path excelTargetFile = Paths.get(String.format("%s/Upload_MyExcel_Korrektur_%06d.xlsx", properties.getWorkdir(), upload.getUploadId()));
        log.info("Processing crm_upload for MyExcel uploadId={} sourceSysten={} crmSystem={}", upload.getUploadId(), upload.getSourceSystem(), upload.getCrmSystem());
        try {
            writeExcelToFile(upload.getContent(), excelSourcefile);

            List<ErrMsg> errors = new ArrayList<>();

            List<MyExcelAccount> myExcelAccounts = new MyExcelReadAccounts().getAccounts(excelSourcefile, errors);
            List<EspoAccount> espoAccounts = MyExcelToEspoAccountMapper.toEspoAccounts(myExcelAccounts);
            VerifyMyExcelForEspo.verifyEspoAccount(myExcelCtx, espoAccounts, errors);

            log.info(String.format("MyExcel %d accounts read, %d errors", espoAccounts.size(), errors.size()));
            log.info(String.format("MyExcel %d accounts mapped, %d errors", espoAccounts.size(), errors.size()));

            List<MyExcelContact> myExcelContacts = new MyExcelReadContacts().getContacts(myExcelAccounts, excelSourcefile, errors);
            List<EspoContact> espoContacts = MyExcelToEspoContactMapper.toEspoContacts(myExcelContacts);
            VerifyMyExcelForEspo.verifyEspoContact(myExcelCtx, espoAccounts, espoContacts, errors);

            log.info(String.format("MyExcel %d contacts read, %d errors", espoContacts.size(), errors.size()));
            log.info(String.format("MyExcel %d contacts mapped, %d errors", espoContacts.size(), errors.size()));

            List<MyExcelLead> myExcelLeads = new MyExcelReadLeads().getLeads(excelSourcefile, errors);
            List<EspoLead> espoLeads = MyExcelToEspoLeadMapper.toEspoLeads(myExcelLeads);
            VerifyMyExcelForEspo.verifyEspoLead(myExcelCtx, espoLeads, errors);

            EspoEntityPool espoEntityPool = new EspoEntityPool();
            espoEntityPool.setAccounts(espoAccounts);
            espoEntityPool.setContacts(espoContacts);

            log.info(String.format("MyExcel %d leads read, %d errors", espoLeads.size(), errors.size()));
            log.info(String.format("MyExcel %d leads mapped, %d errors", espoLeads.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(upload.getCustomerId());
            if (customer.isPresent()) {
                uploadHandlingForEspo.processForEspo(upload, excelSourcefile, excelTargetFile, errors, customer.get(), espoEntityPool);
            } else {
                log.error("Customer not found for customer id={}", upload.getCustomerId());
            }
        } catch (Exception ex) {
            repository.markUploadFailed(upload.getUploadId(), ex.getMessage());
        }
        WorkerUtils.removeFile(excelSourcefile);
    }
}