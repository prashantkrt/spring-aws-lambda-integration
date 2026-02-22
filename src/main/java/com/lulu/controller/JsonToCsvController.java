package com.lulu.controller;

import com.lulu.service.JsonToCsvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JsonToCsvController {

    private final JsonToCsvService jsonToCsvService;

    /**
     * Converts a list of JSON records into CSV and uploads to S3.
     */
    @PostMapping("/json-to-csv")
    public ResponseEntity<String> convertJsonToCsv(@RequestBody List<Map<String, Object>> payload) {
        log.info("Bulk JSON-to-CSV request received | recordCount={}", payload != null ? payload.size() : 0);
        jsonToCsvService.processAndUpload(payload);
        log.info("Bulk JSON-to-CSV completed successfully");
        return ResponseEntity.ok("CSV generated and uploaded to S3 successfully");
    }

    /**
     * Upserts a single JSON record into S3 CSV.
     * Creates file if not present, updates if exists, otherwise inserts.
     * Endpoint: POST /api/v1/json-to-csv/upsert
     */
    @PostMapping("/json-to-csv/upsert")
    public ResponseEntity<String> upsertJsonToCsv(@RequestBody Map<String, Object> payload) {
        log.info("Upsert request received | keys={}", payload != null ? payload.keySet() : "null");
        jsonToCsvService.upsertAndUpload(payload);
        log.info("Upsert completed successfully");
        return ResponseEntity.ok("Record upserted successfully in S3 CSV");
    }
}