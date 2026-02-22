package com.lulu.utility;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

@Slf4j
public class CsvUtil {

    // List<Map> → CSV String
    public static String convertToCsv(List<Map<String, Object>> data) {

        log.info("Starting CSV conversion. Record count: {}",
                data != null ? data.size() : 0);

        if (data == null || data.isEmpty()) {
            log.warn("Received empty or null data for CSV conversion.");
            return "";
        }

        StringWriter writer = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(writer)) {

            // Extract headers
            Set<String> headers = data.get(0).keySet();
            log.debug("Extracted CSV headers: {}", headers);

            csvWriter.writeNext(headers.toArray(new String[0]));

            // Write rows
            for (Map<String, Object> row : data) {

                String[] values = headers.stream()
                        .map(h -> Objects.toString(row.get(h), ""))
                        .toArray(String[]::new);

                csvWriter.writeNext(values);
                log.debug("Written CSV row: {}", Arrays.toString(values));
            }

            log.info("CSV conversion completed successfully.");

        } catch (IOException e) {
            log.error("Error occurred while generating CSV content.", e);
            throw new RuntimeException("Error while generating CSV", e);
        }

        return writer.toString();
    }

    // CSV String → List<Map>
    public static List<Map<String, String>> parseCsv(String csv) {

        log.info("Starting CSV parsing. Input size: {} characters",
                csv != null ? csv.length() : 0);

        List<Map<String, String>> rows = new ArrayList<>();

        if (csv == null || csv.isBlank()) {
            log.warn("Received empty or blank CSV string for parsing.");
            return rows;
        }

        try (CSVReader reader = new CSVReader(new StringReader(csv))) {

            List<String[]> lines = reader.readAll();

            if (lines.isEmpty()) {
                log.warn("CSV file contains no data.");
                return rows;
            }

            // Extract headers
            String[] headers = lines.get(0);
            log.debug("Extracted headers from CSV: {}", Arrays.toString(headers));

            for (int i = 1; i < lines.size(); i++) {

                Map<String, String> row = new LinkedHashMap<>();
                String[] values = lines.get(i);

                for (int j = 0; j < headers.length; j++) {
                    String value = j < values.length ? values[j] : "";
                    row.put(headers[j], value);
                }

                rows.add(row);
                log.debug("Parsed CSV row {}: {}", i, row);
            }
            log.info("CSV parsing completed successfully. Total records: {}", rows.size());

        } catch (Exception e) {
            log.error("Error occurred while parsing CSV content.", e);
            throw new RuntimeException("CSV parse error", e);
        }

        return rows;
    }
}
