package com.crm.app.worker_upload.process;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.dto.Customer;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.worker_common.dto.StatisticsError;
import com.crm.app.worker_common.util.WorkerUtil;
import com.crm.app.worker_upload.dto.StatisticsErrorUploadEspo;
import com.crm.app.worker_upload.dto.StatisticsUploadEspo;
import com.crm.app.worker_upload.error.EspoEntity;
import com.crm.app.worker_upload.error.WorkerUploadException;
import com.crm.app.worker_upload.mail.UploadMailService;
import com.crmmacher.config.BaseCtx;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoAccount;
import com.crmmacher.espo.dto.EspoContact;
import com.crmmacher.espo.dto.EspoEntityPool;
import com.crmmacher.espo.storage_handler.add.AddAccountsToEspo;
import com.crmmacher.espo.storage_handler.add.AddContactsToEspo;
import com.crmmacher.espo.storage_handler.add.AddLeadsToEspo;
import com.crmmacher.espo.storage_handler.add.error.EspoValidationException;
import com.crmmacher.espo.storage_handler.get.GetAccountFromEspo;
import com.crmmacher.espo.storage_handler.get.GetContactFromEspo;
import com.crmmacher.espo.storage_handler.get.GetLeadFromEspo;
import com.crmmacher.espo.storage_handler.sanity_check.SanityCheck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.crmmacher.espo.dto.EspoContact.setContactAccountId;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadHandlingForEspo {

    private final CrmUploadRepositoryPort repository;
    private final UploadMailService uploadMailService;

    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public void processForEspo(CrmUploadContent upload, byte[] excelBytes, List<ErrMsg> errors, Customer customer, EspoEntityPool espoEntityPoolForReceived) {
        EspoEntityPool espoEntityPoolForLoad = new EspoEntityPool();
        EspoEntityPool espoEntityPoolForAdd = new EspoEntityPool();
        EspoEntityPool espoEntityPoolForIgnore = new EspoEntityPool();

        BaseCtx baseCtx = new BaseCtx();
        baseCtx.setBaseUrl(upload.getCrmUrl());
        baseCtx.setApiKey(upload.getApiKey());

        if (!ErrMsg.containsErrors(errors)) {
            List<String> items = new SanityCheck(baseCtx.getBaseUrl(), baseCtx.getApiKey()).checkForExternalReference();
            if (!items.isEmpty()) {
                repository.markUploadFailed(upload.getUploadId(), "SanityCheck failed", GSON.toJson(items.stream()
                        .map(EspoEntity::new)
                        .toList()));
                uploadMailService.sendErrorMailForEspoSanityCheck(customer, upload);
                return;
            }
        } else {
            StatisticsError statisticsError = new StatisticsError();
            statisticsError.setFromErrMsg(errors);
            repository.markUploadFailed(upload.getUploadId(), "Validation failed", GSON.toJson(statisticsError));
            WorkerUtil.markExcelFile(excelBytes, errors);
            uploadMailService.sendExcelHasAlreadyErrorsMailForEspo(customer, upload, errors, WorkerUtil.markExcelFile(excelBytes, errors));
            return;
        }

        if (!ErrMsg.containsErrors(errors)) {
            Instant start = Instant.now();
            try {
                loadEspo(baseCtx, espoEntityPoolForLoad);
                Duration durationEspoLoad = Duration.between(start, Instant.now());

                checkForAddOrIgnore(espoEntityPoolForLoad, espoEntityPoolForReceived, espoEntityPoolForAdd, espoEntityPoolForIgnore);

                start = Instant.now();
                addEntitiesToEspo(baseCtx, espoEntityPoolForAdd);
                Duration durationEspoUpLoad = Duration.between(start, Instant.now());

                logStatistics(espoEntityPoolForReceived, espoEntityPoolForAdd, espoEntityPoolForIgnore);

                StatisticsUploadEspo statisticsUploadEspo = setStatistics(espoEntityPoolForLoad, espoEntityPoolForReceived, espoEntityPoolForAdd, espoEntityPoolForIgnore);
                statisticsUploadEspo.setNSecondsForEspoLoad(durationEspoLoad.getSeconds());
                statisticsUploadEspo.setNSecondsForEspoUpload(durationEspoUpLoad.getSeconds());
                String statisticsJson = GSON.toJson(statisticsUploadEspo);

                repository.markUploadDone(upload.getUploadId(), statisticsJson);
                uploadMailService.sendSuccessMailForEspo(customer, upload, espoEntityPoolForAdd, espoEntityPoolForIgnore);
            } catch (EspoValidationException e) {
                StatisticsErrorUploadEspo statisticsErrorUploadEspo = setErrorStatistics(espoEntityPoolForLoad, espoEntityPoolForReceived, espoEntityPoolForAdd, espoEntityPoolForIgnore);
                uploadMailService.sendMailForEspoUploadError(customer, upload, statisticsErrorUploadEspo, espoEntityPoolForAdd);
                String msg = "ESPO Validation failed[" + e.getMessage() + "]";
                repository.markUploadFailed(upload.getUploadId(), msg, GSON.toJson(statisticsErrorUploadEspo));
            } catch (Exception e) {
                StatisticsErrorUploadEspo statisticsErrorUploadEspo = setErrorStatistics(espoEntityPoolForLoad, espoEntityPoolForReceived, espoEntityPoolForAdd, espoEntityPoolForIgnore);
                uploadMailService.sendMailForEspoUploadError(customer, upload, statisticsErrorUploadEspo, espoEntityPoolForAdd);
                String msg = "ESPO Handling failed[" + e.getMessage() + "]";
                repository.markUploadFailed(upload.getUploadId(), msg, GSON.toJson(statisticsErrorUploadEspo));
            }
        }
    }

    private void loadEspo(BaseCtx baseCtx, EspoEntityPool espoEntityPool) {
        Instant start = Instant.now();
        log.info("Loading espo from " + baseCtx.getBaseUrl() + " ...");
        try {
            espoEntityPool.setAccounts(new GetAccountFromEspo().getAccountsWithDetails(baseCtx));
            espoEntityPool.setContacts(new GetContactFromEspo().getContactsWithDetails(baseCtx));
            espoEntityPool.setLeads(new GetLeadFromEspo().getLeadsWithDetails(baseCtx));
        } catch (Exception e) {
            log.error("Error loading espo", e);
            throw new WorkerUploadException(String.format("Error loading espo %s", e.getMessage()));
        }
        Duration duration = Duration.between(start, Instant.now());
        log.info(String.format("Loaded espo %d accounts, %d contacts, %d leads", espoEntityPool.getAccounts().size(), espoEntityPool.getContacts().size(), espoEntityPool.getLeads().size()));
        log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
    }

    private void checkForAddOrIgnore(EspoEntityPool espoEntityPoolForLoad, EspoEntityPool espoEntityPoolForReceived, EspoEntityPool espoEntityPoolForAdd, EspoEntityPool espoEntityPoolForIgnore) {
        for (EspoAccount espoAccount : espoEntityPoolForReceived.getAccounts()) {
            if (espoEntityPoolForLoad.getAccounts().stream().anyMatch(p -> p.getcExternalReference().equals(espoAccount.getcExternalReference()))) {
                espoEntityPoolForIgnore.getAccounts().add(espoAccount);
                Optional<EspoContact> espoContactMatching = espoEntityPoolForReceived.getContacts().stream().filter(p -> p.getEspoAccount() == espoAccount).findFirst();
                espoContactMatching.ifPresent(espoContact -> espoEntityPoolForIgnore.getContacts().add(espoContact));
            } else {
                espoEntityPoolForAdd.getAccounts().add(espoAccount);
                Optional<EspoContact> espoContactMatching = espoEntityPoolForReceived.getContacts().stream().filter(p -> p.getEspoAccount() == espoAccount).findFirst();
                espoContactMatching.ifPresent(espoContact -> espoEntityPoolForAdd.getContacts().add(espoContact));
            }
        }
    }

    private void addEntitiesToEspo(BaseCtx ctx, EspoEntityPool espoEntityPoolForAdd) {
        Duration duration;
        Instant start;
        if (!espoEntityPoolForAdd.getAccounts().isEmpty()) {
            start = Instant.now();
            log.info("Add accounts ...");
            new AddAccountsToEspo().process(ctx, espoEntityPoolForAdd.getAccounts());
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        }

        if (!espoEntityPoolForAdd.getContacts().isEmpty()) {
            start = Instant.now();
            log.info("Add contacts ...");
            setContactAccountId(espoEntityPoolForAdd.getContacts());
            new AddContactsToEspo().process(ctx, espoEntityPoolForAdd.getContacts());
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        }

        if (!espoEntityPoolForAdd.getLeads().isEmpty()) {
            start = Instant.now();
            log.info("Add leads ...");
            new AddLeadsToEspo().process(ctx, espoEntityPoolForAdd.getLeads());
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        }
    }

    private void logStatistics(EspoEntityPool espoEntityPoolForReceived, EspoEntityPool espoEntityPoolForAdd, EspoEntityPool espoEntityPoolForIgnore) {
        String msg;
        msg = String.format("Accounts received %d", espoEntityPoolForReceived.getAccounts().size());
        log.info(msg);
        msg = String.format("Accounts added %d", espoEntityPoolForAdd.getAccounts().size());
        log.info(msg);
        msg = String.format("Accounts ignored %d", espoEntityPoolForIgnore.getAccounts().size());
        log.info(msg);

        msg = String.format("Contacts received %d", espoEntityPoolForReceived.getContacts().size());
        log.info(msg);
        msg = String.format("Contacts added %d", espoEntityPoolForAdd.getContacts().size());
        log.info(msg);
        msg = String.format("Contacts ignored %d", espoEntityPoolForIgnore.getContacts().size());
        log.info(msg);

        msg = String.format("Leads received %d", espoEntityPoolForReceived.getLeads().size());
        log.info(msg);
        msg = String.format("Leads added %d", espoEntityPoolForAdd.getLeads().size());
        log.info(msg);
        msg = String.format("Leads ignored %d", espoEntityPoolForIgnore.getLeads().size());
        log.info(msg);
    }

    private StatisticsUploadEspo setStatistics(EspoEntityPool espoEntityPoolForLoad, EspoEntityPool espoEntityPoolForReceived, EspoEntityPool espoEntityPoolForAdd, EspoEntityPool espoEntityPoolForIgnore) {
        StatisticsUploadEspo statisticsUploadEspo = new StatisticsUploadEspo();

        statisticsUploadEspo.setNAccountsInCrm(espoEntityPoolForLoad.getAccounts().size());
        statisticsUploadEspo.setNAccountsReceived(espoEntityPoolForReceived.getAccounts().size());
        statisticsUploadEspo.setNAccountsAdded(espoEntityPoolForAdd.getAccounts().size());
        statisticsUploadEspo.setNAccountsIgnored(espoEntityPoolForIgnore.getAccounts().size());

        statisticsUploadEspo.setNContactsInCrm(espoEntityPoolForLoad.getContacts().size());
        statisticsUploadEspo.setNContactsReceived(espoEntityPoolForReceived.getContacts().size());
        statisticsUploadEspo.setNContactsAdded(espoEntityPoolForAdd.getContacts().size());
        statisticsUploadEspo.setNContactsIgnored(espoEntityPoolForIgnore.getContacts().size());

        return statisticsUploadEspo;
    }

    private StatisticsErrorUploadEspo setErrorStatistics(EspoEntityPool espoEntityPoolForLoad, EspoEntityPool espoEntityPoolForReceived, EspoEntityPool espoEntityPoolForAdd, EspoEntityPool espoEntityPoolForIgnore) {
        StatisticsErrorUploadEspo statisticsErrorUploadEspo = new StatisticsErrorUploadEspo();

        statisticsErrorUploadEspo.setNAccountsInCrm(espoEntityPoolForLoad.getAccounts().size());
        statisticsErrorUploadEspo.setNAccountsReceived(espoEntityPoolForReceived.getAccounts().size());
        statisticsErrorUploadEspo.setNAccountsMeantToBeAdded(espoEntityPoolForAdd.getAccounts().size());
        statisticsErrorUploadEspo.setNAccountsAdded(espoEntityPoolForAdd.getAccounts().stream().filter(EspoAccount::isAddedToEspo).count());
        statisticsErrorUploadEspo.setNAccountsRejected(espoEntityPoolForAdd.getAccounts().stream().filter(EspoAccount::isRejectedByEspo).count());
        statisticsErrorUploadEspo.setNAccountsIgnored(espoEntityPoolForIgnore.getAccounts().size());

        statisticsErrorUploadEspo.setNContactsInCrm(espoEntityPoolForLoad.getContacts().size());
        statisticsErrorUploadEspo.setNContactsReceived(espoEntityPoolForReceived.getContacts().size());
        statisticsErrorUploadEspo.setNContactsMeantToBeAdded(espoEntityPoolForAdd.getContacts().size());
        statisticsErrorUploadEspo.setNContactsAdded(espoEntityPoolForAdd.getContacts().stream().filter(EspoContact::isAddedToEspo).count());
        statisticsErrorUploadEspo.setNContactsRejected(espoEntityPoolForAdd.getContacts().stream().filter(EspoContact::isRejectedByEspo).count());
        statisticsErrorUploadEspo.setNContactsIgnored(espoEntityPoolForIgnore.getContacts().size());

        return statisticsErrorUploadEspo;
    }

}
