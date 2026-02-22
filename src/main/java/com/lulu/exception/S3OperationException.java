package com.lulu.exception;

public class S3OperationException extends DataIntegrationException {
    public S3OperationException(String message) {
        super(message, ErrorCode.S3_OPERATION_FAILED);
    }
}
