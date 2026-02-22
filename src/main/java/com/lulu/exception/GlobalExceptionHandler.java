package com.lulu.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("No handler found | path={}", request.getRequestURI());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                "Resource not found",
                request
        );
    }

    @ExceptionHandler(DataIntegrationException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleCustomException(DataIntegrationException ex, HttpServletRequest request) {
        HttpStatus status = resolveStatus(ex.getErrorCode());
        logByStatus(status, ex, request);
        return buildResponse(
                status,
                ex.getErrorCode(),
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception | path={}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.GENERIC_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Internal server error",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse<Void>> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request) {

        ApiErrorResponse<Void> response = ApiErrorResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(errorCode.name())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, status);
    }

    private HttpStatus resolveStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_PAYLOAD -> HttpStatus.BAD_REQUEST;
            case MISSING_FIELD -> HttpStatus.BAD_REQUEST;
            case INVALID_FORMAT -> HttpStatus.BAD_REQUEST;
            case S3_FILE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CSV_PROCESSING_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case S3_OPERATION_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private void logByStatus(HttpStatus status, DataIntegrationException ex, HttpServletRequest request) {

        if (status.is4xxClientError()) {
            log.warn("{} | path={} | errorCode={} | message={}",
                    ex.getClass().getSimpleName(),
                    request.getRequestURI(),
                    ex.getErrorCode(),
                    ex.getMessage());
        } else {
            log.error("{} | path={} | errorCode={} | message={}",
                    ex.getClass().getSimpleName(),
                    request.getRequestURI(),
                    ex.getErrorCode(),
                    ex.getMessage(),
                    ex);
        }
    }
}
