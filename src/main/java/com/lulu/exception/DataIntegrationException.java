package com.lulu.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataIntegrationException extends RuntimeException {
    private final ErrorCode errorCode;

    public DataIntegrationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
