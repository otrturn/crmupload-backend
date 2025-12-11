package com.crm.app.worker_upload.process;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.port.customer.Customer;
import com.crm.app.worker_upload.mail.UploadMailService;
import com.crmmacher.bexio_excel.error.BexioReaderException;
import com.crmmacher.config.BaseCtx;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoAccount;
import com.crmmacher.espo.dto.EspoContact;
import com.crmmacher.espo.dto.EspoEntityPool;
import com.crmmacher.espo.storage_handler.add.AddAccountsToEspo;
import com.crmmacher.espo.storage_handler.add.AddContactsToEspo;
import com.crmmacher.espo.storage_handler.add.AddLeadsToEspo;
import com.crmmacher.espo.storage_handler.get.GetAccountFromEspo;
import com.crmmacher.espo.storage_handler.get.GetContactFromEspo;
import com.crmmacher.espo.storage_handler.get.GetLeadFromEspo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final String FORMAT_STRING = "Duration: %02d:%02d:%02d";

    public void processForEspo(CrmUploadContent upload, byte[] excelBytes, Path excelTargetfile, List<ErrMsg> errors, Customer customer, EspoEntityPool espoEntityPoolForReceived) {
        EspoEntityPool espoEntityPoolForLoad = new EspoEntityPool();
        EspoEntityPool espoEntityPoolForAdd = new EspoEntityPool();
        EspoEntityPool espoEntityPoolForIgnore = new EspoEntityPool();

        BaseCtx baseCtx = new BaseCtx();
        baseCtx.setBaseUrl(upload.getCrmUrl());
        baseCtx.setApiKey(upload.getApiKey());

        if (!ErrMsg.containsErrors(errors)) {
            loadEspo(baseCtx, espoEntityPoolForLoad);
            checkForAddOrIgnore(espoEntityPoolForLoad, espoEntityPoolForReceived, espoEntityPoolForAdd, espoEntityPoolForIgnore);
            addEntitiesToEspo(baseCtx, espoEntityPoolForAdd);
            logStatistics(espoEntityPoolForReceived, espoEntityPoolForAdd, espoEntityPoolForIgnore);

            repository.markUploadDone(upload.getUploadId());
            uploadMailService.sendSuccessMailForEspo(customer, upload, espoEntityPoolForAdd);
        } else {
            repository.markUploadFailed(upload.getUploadId(), "Validation failed");
            markExcelFile(excelBytes, excelTargetfile, errors);
            uploadMailService.sendErrorMailForEspo(customer, upload, errors, excelTargetfile);
        }
    }

    private void markExcelFile(byte[] excelBytes, Path excelTargetfile, List<ErrMsg> errors) {
        try (InputStream fis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(fis);
             OutputStream os = Files.newOutputStream(excelTargetfile)) {
            colourCells(errors, workbook);
            workbook.write(os);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BexioReaderException("Cannot process excel files [byteArray][" + excelTargetfile + "]");
        }
    }

    private static void colourCells(List<ErrMsg> errors, Workbook workbook) {
        XSSFCellStyle cellStyleMarkedCell = (XSSFCellStyle) workbook.createCellStyle();
        cellStyleMarkedCell.setFillForegroundColor(IndexedColors.RED.index);
        cellStyleMarkedCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (ErrMsg errMsg : errors) {
            Sheet sheet = workbook.getSheetAt(errMsg.getSheetNum());
            Row row = sheet.getRow(errMsg.getRowNum());
            Cell cell = row.getCell(errMsg.getColNum());
            if (cell == null) {
                cell = row.createCell(errMsg.getColNum());
            }

            cell.setCellStyle(cellStyleMarkedCell);
            addComment(cell, errMsg.getMessage());
        }
    }

    public static void addComment(Cell cell, String text) {
        if (cell == null || text == null) {
            return;
        }

        Sheet sheet = cell.getSheet();
        Workbook wb = sheet.getWorkbook();
        CreationHelper factory = wb.getCreationHelper();

        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRowIndex());
        anchor.setCol2(cell.getColumnIndex() + 3);
        anchor.setRow2(cell.getRowIndex() + 3);

        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));

        cell.setCellComment(comment);
    }

    private void loadEspo(BaseCtx baseCtx, EspoEntityPool espoEntityPool) {
        Instant start = Instant.now();
        log.info("Loading espo ...");
        espoEntityPool.setAccounts(new GetAccountFromEspo().getAccountsWithDetails(baseCtx));
        espoEntityPool.setContacts(new GetContactFromEspo().getContactsWithDetails(baseCtx));
        espoEntityPool.setLeads(new GetLeadFromEspo().getLeadsWithDetails(baseCtx));
        Duration duration = Duration.between(start, Instant.now());
        log.info(String.format("Loaded espo %d accounts, %d contacts, %d leads", espoEntityPool.getAccounts().size(), espoEntityPool.getContacts().size(), espoEntityPool.getLeads().size()));
        log.info(String.format(FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
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
            log.info(String.format(FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        }

        if (!espoEntityPoolForAdd.getContacts().isEmpty()) {
            start = Instant.now();
            log.info("Add contacts ...");
            setContactAccountId(espoEntityPoolForAdd.getContacts());
            new AddContactsToEspo().process(ctx, espoEntityPoolForAdd.getContacts());
            duration = Duration.between(start, Instant.now());
            log.info(String.format(FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        }

        if (!espoEntityPoolForAdd.getLeads().isEmpty()) {
            start = Instant.now();
            log.info("Add leads ...");
            new AddLeadsToEspo().process(ctx, espoEntityPoolForAdd.getLeads());
            duration = Duration.between(start, Instant.now());
            log.info(String.format(FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
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

}
