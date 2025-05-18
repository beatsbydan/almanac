package com.project.server.dtos.api;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
public class ApiError {
    private String message;

    private HttpStatus status;

    private ZonedDateTime timeStamp;

    private Map<String, String> errors;

    public ApiError(String message, HttpStatus httpStatus, ZonedDateTime timeStamp) {
        this.message = message;
        this.status = httpStatus;
        this.timeStamp = timeStamp;
    }
    public ApiError(String message, HttpStatus httpStatus, ZonedDateTime timeStamp, Map<String, String> errors) {
        this.message = message;
        this.status = httpStatus;
        this.timeStamp = timeStamp;
        this.errors = errors;
    }
}
