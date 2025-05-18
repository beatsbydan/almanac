package com.project.server.configs;

import com.project.server.dtos.api.ApiError;
import com.project.server.dtos.api.ApiResponse;
import com.project.server.exceptions.*;
import lombok.NonNull;
import org.springframework.http.converter.HttpMessageConverter;
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

@ControllerAdvice
public class Advice implements ResponseBodyAdvice<Object> {
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

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e){
        return new ResponseEntity<>(buildError(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
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
}
