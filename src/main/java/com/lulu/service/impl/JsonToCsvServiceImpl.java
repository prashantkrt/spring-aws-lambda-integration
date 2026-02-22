package com.lulu.service.impl;

import com.lulu.service.JsonToCsvService;
import com.lulu.service.S3Service;
import com.lulu.utility.CsvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonToCsvServiceImpl implements JsonToCsvService {

    private final S3Service s3Service;

    @Value("${app.s3.output-folder}")
    private String outputFolder;

    @Value("${app.s3.file-name}")
    private String fileName;

    @Override
    public void processAndUpload(List<Map<String, Object>> data) {

        log.info("Starting bulk JSON to CSV conversion. Records count: {}",
                data != null ? data.size() : 0);

        if (data == null || data.isEmpty()) {
            log.warn("Received empty JSON payload for bulk upload");
            throw new IllegalArgumentException("Empty JSON payload");
        }

        String csvContent = CsvUtil.convertToCsv(data);
        log.info("CSV conversion successful. Uploading to S3...");
        s3Service.uploadCsv(csvContent);
        log.info("Bulk CSV upload completed successfully.");
    }

    @Override
    public void upsertAndUpload(Map<String, Object> record) {

        log.info("Starting upsert operation for record with id: {}", record.get("id"));

        if (record == null || record.isEmpty()) {
            log.warn("Received empty JSON payload for upsert");
            throw new IllegalArgumentException("Empty JSON payload");
        }

        String key = String.format("%s/%s", outputFolder, fileName);

        List<Map<String, String>> rows;

        try {
            log.info("Reading existing CSV from S3. Key: {}", key);
            String existingCsv = s3Service.readFile(key);
            rows = CsvUtil.parseCsv(existingCsv);
            log.info("Existing CSV loaded. Total records: {}", rows.size());
        } catch (Exception e) {
            log.warn("CSV file not found. Creating new file with incoming record.");
            String csvContent = CsvUtil.convertToCsv(List.of(record));
            s3Service.uploadCsv(csvContent);
            log.info("New CSV file created successfully.");
            return;
        }

        // Convert incoming record to Map<String, String>
        Map<String, String> newRecord = new LinkedHashMap<>();
        record.forEach((k, v) ->
                newRecord.put(k, Objects.toString(v, "")));

        boolean updated = false;
        for (Map<String, String> row : rows) {

            if (Objects.equals(row.get("id"), newRecord.get("id"))) {
                log.info("Matching record found for id: {}. Updating existing row.",
                        newRecord.get("id"));
                newRecord.forEach((K,V)->record.put(K,V));
                updated = true;
                break;
            }
        }

        if (!updated) {
            log.info("No existing record found for id: {}. Inserting new row.",
                    newRecord.get("id"));
            rows.add(newRecord);
        }

        List<Map<String, Object>> objectRows = rows.stream()
                .map(row -> new LinkedHashMap<String, Object>(row))
                .collect(Collectors.toList());

        String updatedCsv = CsvUtil.convertToCsv(objectRows);
        log.info("Uploading updated CSV to S3. Total records after upsert: {}", rows.size());
        s3Service.uploadCsv(updatedCsv);
        log.info("Upsert operation completed successfully for id: {}", newRecord.get("id"));
    }
}
