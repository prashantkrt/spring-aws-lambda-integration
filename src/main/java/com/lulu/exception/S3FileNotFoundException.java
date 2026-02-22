package com.lulu.exception;

public class S3FileNotFoundException extends DataIntegrationException {
    public S3FileNotFoundException(String message) {
        super(message, ErrorCode.S3_FILE_NOT_FOUND);
    }
}
