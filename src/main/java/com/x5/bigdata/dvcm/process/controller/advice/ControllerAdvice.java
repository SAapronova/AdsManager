package com.x5.bigdata.dvcm.process.controller.advice;


import com.x5.bigdata.dvcm.process.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public BaseResponse<Void> handle(HttpServletRequest req, Exception e) {
        log.error("INTERNAL_SERVER_ERROR, URI: {}, params: {}", req.getRequestURI(), getParams(req), e);
        return BaseResponse.fail(new ServerError(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ValidationException.class})
    public BaseResponse<ValidationError> handleValidationException(HttpServletRequest req, ValidationException e) {
        log.warn("BAD_REQUEST_Validation, URI: {}, params: {}, message: {}",
                req.getRequestURI(), getParams(req), e.getMessage());

        return BaseResponse.fail(new ValidationError(e.getItems()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoSuchElementException.class})
    public BaseResponse<ValidationError> handleValidationException(HttpServletRequest req, NoSuchElementException e) {
        log.warn("NOT_FOUND, URI: {}, params: {}", req.getRequestURI(), getParams(req), e);
        return BaseResponse.fail(new BaseError(ErrorType.NOT_FOUND_ERROR));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public BaseResponse<NotFoundError> handleValidationException(HttpServletRequest req, NotFoundException e) {
        log.warn("NOT_FOUND, URI: {}, params: {}", req.getRequestURI(), getParams(req), e);
        return BaseResponse.fail(new NotFoundError(e.getItems()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<String> accessDeniedHandle(HttpServletRequest req, AccessDeniedException e) {
        log.error("ACCESS_DENIED, URI: {}, params: {}", req.getRequestURI(), getParams(req), e);
        return BaseResponse.fail(new ServerError(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponse<String> invalidMethodArgumentExceptionHandler(HttpServletRequest req, MethodArgumentTypeMismatchException e) {
        log.warn("BAD_REQUEST_MethodArgumentTypeMismatch, URI: {}, params: {}, message: {}",
                req.getRequestURI(), getParams(req), e.getMessage());
        return BaseResponse.fail(new BaseError(ErrorType.ARGUMENT_TYPE_ERROR, String.format(
                "Недопустимое значение параметра, URI: %s, params: %s", req.getRequestURI(), getParams(req))));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<String> invalidMethodArgumentNotValidExceptionHandler(HttpServletRequest req, MethodArgumentNotValidException e) {
        log.warn("BAD_REQUEST_MethodArgumentNotValid, URI: {}, params: {}, message: {}",
                req.getRequestURI(), getParams(req), e.getMessage());

        List<ValidationItem> errors = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String value = ((FieldError) error).getRejectedValue().toString();
                    String errorMessage = error.getDefaultMessage();
                    return new ValidationItem(fieldName, value, errorMessage);
                }).collect(Collectors.toList());
        return BaseResponse.fail(new ValidationError(errors));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<String> invalidConstraintExceptionHandler(HttpServletRequest req, ConstraintViolationException e) {
        log.warn("BAD_REQUEST_ConstraintViolation, URI: {}, params: {}, message: {}",
                req.getRequestURI(), getParams(req), e.getMessage());
        List<ValidationItem> errors = e.getConstraintViolations()
                .stream()
                .map(error -> {
                    String fieldName = error.getPropertyPath().toString();
                    Object field = error.getInvalidValue();
                    String value = field.toString();
                    if (field instanceof MultipartFile && fieldName != null) {
                        if (fieldName.endsWith(".size")) {
                            value = String.valueOf(((MultipartFile) field).getSize());
                        } else {
                            value = ((MultipartFile) field).getOriginalFilename()
                                    .substring(((MultipartFile) field).getOriginalFilename().lastIndexOf(".") + 1);
                        }
                    }

                    String errorMessage = error.getMessage();
                    return new ValidationItem(fieldName, value, errorMessage);
                }).collect(Collectors.toList());
        return BaseResponse.fail(new ValidationError(errors));
    }

    private String getParams(HttpServletRequest req) {
        Map<String, String[]> map = req.getParameterMap();
        return map.keySet().stream()
                .map(key -> key + "=" + Arrays.toString(map.get(key)))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
