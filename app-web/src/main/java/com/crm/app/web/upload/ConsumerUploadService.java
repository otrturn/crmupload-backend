package com.crm.app.web.upload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerUploadService {

    private final JdbcTemplate jdbcTemplate;

    public void handleUpload(UploadRequest request, MultipartFile zipFile) {
        try {
            // 1) Excel aus ZIP extrahieren
            byte[] excelBytes = unzipSingleEntry(zipFile);

            // 2) consumer_id über E-Mail bestimmen
            long consumerId = findConsumerIdByEmail(request.emailAddress());

            // 3) upload_id aus Sequence holen
            long uploadId = nextUploadId();

            // 4) Datensatz in consumer_upload einfügen
            insertConsumerUpload(uploadId, consumerId, excelBytes);

            log.info("Upload stored: upload_id={}, consumer_id={}, email={}",
                    uploadId, consumerId, request.emailAddress());

            // crmCustomerId und crmApiKey könntest du hier z.B. für spätere
            // Weiterverarbeitung loggen oder separat speichern.
            log.debug("CRM meta: customerId={}, apiKey(length)={}",
                    request.crmCustomerId(),
                    request.crmApiKey() != null ? request.crmApiKey().length() : 0);

        } catch (IOException e) {
            log.error("Error while processing uploaded ZIP file", e);
            throw new IllegalStateException("Could not read uploaded file", e);
        }
    }

    private byte[] unzipSingleEntry(MultipartFile zipFile) throws IOException {
        if (zipFile.isEmpty()) {
            throw new IllegalStateException("Uploaded ZIP file is empty");
        }

        try (InputStream is = zipFile.getInputStream();
             ZipInputStream zis = new ZipInputStream(is);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Erste Datei holen
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                throw new IllegalStateException("ZIP archive contains no entries");
            }

            String name = entry.getName().toLowerCase();

            // 1) Sicherstellen, dass es eine Excel-Datei .xlsx ist
            if (!name.endsWith(".xlsx")) {
                throw new IllegalStateException("ZIP must contain exactly one .xlsx file (found: " + entry.getName() + ")");
            }

            // Datei extrahieren
            byte[] buffer = new byte[8192];
            int len;
            while ((len = zis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            zis.closeEntry();

            // 2) Sicherstellen, dass NUR eine Datei drin ist
            ZipEntry secondEntry = zis.getNextEntry();
            if (secondEntry != null) {
                throw new IllegalStateException("ZIP archive must contain exactly one file, but found multiple entries");
            }

            return baos.toByteArray();
        }
    }

    private long findConsumerIdByEmail(String email) {
        try {
            Long id = jdbcTemplate.queryForObject(
                    "SELECT consumer_id FROM app.consumer WHERE email_address = ?",
                    Long.class,
                    email
            );

            return Objects.requireNonNull(id,
                    "Consumer ID unexpectedly null for email: " + email);

        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalStateException("No consumer found for email: " + email);
        }
    }

    private long nextUploadId() {
        Long id = jdbcTemplate.queryForObject(
                "SELECT nextval('app.sequence_consumer_upload')",
                Long.class
        );
        if (id == null) {
            throw new IllegalStateException("Could not obtain next upload_id from sequence");
        }
        return id;
    }

    private void insertConsumerUpload(long uploadId, long consumerId, byte[] content) {
        int updated = jdbcTemplate.update(
                "INSERT INTO app.consumer_upload (upload_id, consumer_id, content, status) VALUES (?, ?, ?, 'new')",
                ps -> {
                    ps.setLong(1, uploadId);
                    ps.setLong(2, consumerId);
                    ps.setBytes(3, content);
                }
        );

        if (updated != 1) {
            throw new IllegalStateException("Insert into app.consumer_upload affected " + updated + " rows");
        }
    }
}
