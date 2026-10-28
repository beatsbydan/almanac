package com.project.server.configs;

import com.project.server.dtos.api.ApiError;
import com.project.server.dtos.api.ApiResponse;
import com.project.server.exceptions.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class Advice implements ResponseBodyAdvice<Object> {
    /**
     * Every @Valid-annotated request body in the API throws this on a failed field constraint —
     * without a handler for it, it fell through to handleGenericException below and came back as
     * a 500 with Spring's internal "Validation failed for argument..." message, rather than the
     * 400 + field-level breakdown ApiError.errors was already shaped for but nothing populated.
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return new ResponseEntity<>(buildError("Validation failed.", HttpStatus.BAD_REQUEST, fieldErrors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = DuplicateEntryException.class)
    public ResponseEntity<Object> handleDuplicateEntryException(DuplicateEntryException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = FileGenerationException.class)
    public ResponseEntity<Object> handleFileGenerationException(FileGenerationException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = MailingException.class)
    public ResponseEntity<Object> handleMailingException(MailingException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = RequestValidationException.class)
    public ResponseEntity<Object> handleRequestGenerationException(RequestValidationException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = ScheduleUpsertException.class)
    public ResponseEntity<Object> handleScheduleUpsertException(ScheduleUpsertException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(value = TimeTableGenerationException.class)
    public ResponseEntity<Object> handleTimeTableGenerationException(TimeTableGenerationException e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    /**
     * Catches whatever none of the specific handlers above anticipated. Returning e.getMessage()
     * straight to the client (the previous behavior) leaks internals a caller has no business
     * seeing — a DataIntegrityViolationException's message can include table/column/constraint
     * names, for one. Log the real exception (tenant-tagged, see TenantContext) and return a
     * message safe for any caller instead.
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e){
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return new ResponseEntity<>(buildError("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        String path = returnType.getDeclaringClass().getPackageName();

        return !path.contains("org.springdoc") || !byte[].class.equals(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        String path = request.getURI().getPath();

        if (path.startsWith("/v3/api-docs") && path.startsWith("/swagger-ui")) {
            return body;
        }

        if(body instanceof org.springframework.core.io.Resource){
            return body;
        }

        if(body instanceof  byte[]) return body;

        return body instanceof ApiResponse ? body : new ApiResponse(true, body);
    }

    private ApiError buildError(String message, HttpStatus status) {
        return new ApiError(message, status, ZonedDateTime.now(ZoneId.of("Z")));
    }

    private ApiError buildError(String message, HttpStatus status, Map<String, String> errors) {
        return new ApiError(message, status, ZonedDateTime.now(ZoneId.of("Z")), errors);
    }
}
