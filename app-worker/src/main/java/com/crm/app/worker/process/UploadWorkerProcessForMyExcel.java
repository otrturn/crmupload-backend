package com.crm.app.worker.process;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.worker.config.ConsumerUploadProperties;
import com.crm.app.worker.mail.UploadMailService;
import com.crm.app.worker.util.WorkerUtils;
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

import static com.crm.app.worker.util.WorkerUtils.writeExcelToFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadWorkerProcessForMyExcel {

    private final ConsumerUploadRepositoryPort repository;
    private final ConsumerUploadProperties properties;
    private final UploadMailService uploadMailService;

    private final MyExcelCtx myExcelCtx;

    public void processUpload(ConsumerUploadContent upload) {
        Path excelFile = Paths.get(String.format("%s/Upload_MyExcel_%06d.xlsx", properties.getWorkdir(), upload.uploadId()));
        log.info("Processing consumer_upload for MyExcel uploadId={} sourceSysten={} crmSystem={}", upload.uploadId(), upload.sourceSystem(), upload.crmSystem());
        try {
            writeExcelToFile(upload.content(), excelFile);

            List<ErrMsg> errors = new ArrayList<>();

            List<MyExcelAccount> myExcelAccounts = new MyExcelReadAccounts().getAccounts(excelFile, errors);
            List<EspoAccount> espoAccounts = MyExcelToEspoAccountMapper.toEspoAccounts(myExcelAccounts);
            VerifyMyExcelForEspo.verifyEspoAccount(myExcelCtx, espoAccounts, errors);

            log.info(String.format("MyExcel %d accounts read, %d errors", espoAccounts.size(), errors.size()));
            log.info(String.format("MyExcel %d accounts mapped, %d errors", espoAccounts.size(), errors.size()));

            List<MyExcelContact> myExcelContacts = new MyExcelReadContacts().getContacts(myExcelAccounts, excelFile, errors);
            List<EspoContact> espoContacts = MyExcelToEspoContactMapper.toEspoContacts(myExcelContacts);
            VerifyMyExcelForEspo.verifyEspoContact(myExcelCtx, espoAccounts, espoContacts, errors);

            log.info(String.format("MyExcel %d contacts read, %d errors", espoContacts.size(), errors.size()));
            log.info(String.format("MyExcel %d contacts mapped, %d errors", espoContacts.size(), errors.size()));

            List<MyExcelLead> myExcelLeads = new MyExcelReadLeads().getLeads(excelFile, errors);
            List<EspoLead> espoLeads = MyExcelToEspoLeadMapper.toEspoLeads(myExcelLeads);
            VerifyMyExcelForEspo.verifyEspoLead(myExcelCtx, espoLeads, errors);

            EspoEntityPool espoEntityPool = new EspoEntityPool();
            espoEntityPool.setAccounts(espoAccounts);
            espoEntityPool.setContacts(espoContacts);

            log.info(String.format("MyExcel %d leads read, %d errors", espoLeads.size(), errors.size()));
            log.info(String.format("MyExcel %d leads mapped, %d errors", espoLeads.size(), errors.size()));

            if (!ErrMsg.containsErrors(errors)) {
                repository.markUploadDone(upload.uploadId());
                Optional<Consumer> consumer = repository.findConsumerByConsumerId(upload.consumerId());
                if (consumer.isPresent()) {
                    uploadMailService.sendSuccessMailForEspo(consumer.get(), upload, espoEntityPool);
                } else {
                    log.error("Consumer not found for consumer id={}", upload.consumerId());
                }
            } else {
                repository.markUploadFailed(upload.uploadId(), "Validation failed");
            }
        } catch (Exception ex) {
            repository.markUploadFailed(upload.uploadId(), ex.getMessage());
        }
        WorkerUtils.removeFile(excelFile);
    }
}