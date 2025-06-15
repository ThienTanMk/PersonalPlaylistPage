package com.example.Playlist.exception;



import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.Playlist.dto.ApiResponse;

import java.util.Map;
import java.util.Objects;

    @ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    final static String MIN_ATTRIBUTE = "min";
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException appException) {
        ApiResponse apiResponse = new ApiResponse();
        ErrorCode errorCode = appException.getErrorCode();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return  ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException runtimeException) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED.getCode());
        apiResponse.setMessage(runtimeException.getMessage());
        return  ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode;
        Map<String, Objects> attributes = null;
        ApiResponse apiResponse = new ApiResponse();;
        try {
            errorCode  = ErrorCode.valueOf(exception.getFieldError().getDefaultMessage());
            apiResponse.setCode(errorCode.getCode());
            var constraintViolation = exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            apiResponse.setMessage(Objects.nonNull(attributes) ? mapAttribute(errorCode.getMessage(), attributes) : errorCode.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            apiResponse.setCode(ErrorCode.INVALID_KEY.getCode());
            apiResponse.setMessage(ErrorCode.INVALID_KEY.getMessage());
        }
        return  ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessdeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return  ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = AuthorizationServiceException.class)
    ResponseEntity<ApiResponse> handlingAuthorizationException(AuthorizationServiceException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        return  ResponseEntity.status(errorCode.getCode()).body(ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    private String mapAttribute(String message, Map<String, Objects> attributes) {
        String min = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", min);
    }
}
