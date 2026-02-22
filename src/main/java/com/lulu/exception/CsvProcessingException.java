package com.lulu.exception;

public class CsvProcessingException extends DataIntegrationException {
    public CsvProcessingException(String message) {
        super(message, ErrorCode.CSV_PROCESSING_ERROR);
    }
}
