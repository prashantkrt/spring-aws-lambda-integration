package com.lulu.exception;

public class InvalidPayloadException extends DataIntegrationException{
    public InvalidPayloadException(String message) {
        super(message, ErrorCode.INVALID_PAYLOAD);
    }
}
