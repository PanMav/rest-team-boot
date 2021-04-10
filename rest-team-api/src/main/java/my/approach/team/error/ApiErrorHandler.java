package my.approach.team.error;

import my.approach.team.model.dto.ApiError;
import my.approach.team.model.dto.ApiErrorResponse;
import my.approach.team.util.Util;
import my.approach.team.idm.nx.token.client.commons.ApiKeyExchange;
import my.approach.team.idm.nx.token.client.commons.TokenClientException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@Slf4j
@RestControllerAdvice
        (assignableTypes = {})
public class ApiErrorHandler {
    @ExceptionHandler(UnauthorizedApiAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedApiAccessException(UnauthorizedApiAccessException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder().code(ApiErrorCode.SGM_E_ACCESS_FORBIDDEN.name()).message(exc.getLocalizedMessage()).build()))
                        .build());
    }

    @ExceptionHandler(ApiKeyExchange.ApiKeyExchangeException.class)
    public ResponseEntity<ApiErrorResponse> handleApiKeyExchangeException(ApiKeyExchange.ApiKeyExchangeException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder()
                                .code(ApiErrorCode.SGM_E_API_COMM_FAILURE.name())
                                .message(exc.getLocalizedMessage())
                                .extraInfo(exc.getLocalizedMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(TokenClientException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenClientException(TokenClientException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder().code(ApiErrorCode.SGM_E_INVALID_TOKEN.name()).message("Invalid access token provided").build()))
                        .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder().code(ApiErrorCode.SGM_E_ILLEGAL_OPERATION.name()).message(exc.getLocalizedMessage()).build()))
                        .build());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<ApiError> errors = exc.getFieldErrors().stream().map(error -> {
            Class<?> fieldType = exc.getFieldType(error.getField());
            String message = String.format(
                    "Invalid request parameter \"%s\" with value: \"%s\". Expected type: \"%s\"",
                    error.getField(), error.getRejectedValue(), fieldType != null ? fieldType.getTypeName() : "unknown"
            );
            return ApiError.builder()
                    .code(ApiErrorCode.SGM_E_INVALID_PARAM.name())
                    .message(message)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(errors)
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder()
                                .code(ApiErrorCode.SGM_E_UNAUTHORIZED.name())
                                .message("Unauthorized access to resource, or action is not permitted.")
                                .build()))
                        .build());
    }

    @ExceptionHandler(PaginationNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handlePaginationNotSupportedException(PaginationNotSupportedException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder().code(ApiErrorCode.SGM_E_PAGINATION_NOT_SUPPORTED.name()).message(exc.getLocalizedMessage()).build()))
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder().code(ApiErrorCode.SGM_E_GENERIC_ERROR.name()).message(exc.getLocalizedMessage()).build()))
                        .build());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccessException(DataAccessException exc) {
        log.error(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(
                                ApiError.builder()
                                        .code(ApiErrorCode.SGM_E_GENERIC_ERROR.name())
                                        .message("Error while accessing/writing data, "+ exc.getLocalizedMessage())
                                        .build()
                        ))
                        .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exc) {
        log.error(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(
                                ApiError.builder()
                                        .code(ApiErrorCode.SGM_E_ILLEGAL_OPERATION.name())
                                        .message("Requested operation is not supported")
                                        .build()
                        ))
                        .build());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(EntityNotFoundException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        final HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity
                .status(status)
                .body(
                        ApiErrorResponse.builder()
                                .status(status.value())
                                .errors(singletonList(
                                        ApiError.builder()
                                                .code(ApiErrorCode.SGM_E_RESOURCE_NOT_FOUND.name())
                                                .message(exc.getLocalizedMessage())
                                                .build()
                                )).build()
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.BAD_REQUEST;






        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(exc.getConstraintViolations().stream().map(violation ->
                                ApiError.builder()
                                .extraInfo("No additional info")
                                .code(Util.getApiErrorCode(violation.getPropertyPath().toString()))
                                .message(violation.getMessage())
                                .build())
                                .collect(Collectors.toList()))
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exc) {
        log.warn(exc.getLocalizedMessage(), exc);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(
                                ApiError.builder()
                                        .code(ApiErrorCode.SGM_E_INVALID_PARAM.name())
                                        .message("Request contained unacceptable arguments, " + exc.getMessage())
                                        .build()
                        ))
                        .build());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(ValidationException exc) {
        log.error(exc.getLocalizedMessage(), exc);

        Throwable rootCause = exc;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(
                                ApiError.builder()
                                        .code(ApiErrorCode.SGM_E_GENERIC_ERROR.name())
                                        .message("This is an unexpected error. You should report this")
                                        .build()
                        ))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exc) {
        log.error(exc.getLocalizedMessage(), exc); // Log it as ERROR - it probably needs handling
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .errors(singletonList(ApiError.builder().code(ApiErrorCode.SGM_E_GENERIC_ERROR.name()).message("Internal Error").build()))
                        .build());
    }
}
