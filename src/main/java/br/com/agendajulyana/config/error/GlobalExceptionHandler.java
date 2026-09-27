package br.com.agendajulyana.config.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        var status = ex.getStatusCode();
        return response(status.value(), status.getReasonPhrase(), codeFor(status.value()),
                ex.getReason() != null ? ex.getReason() : "A requisição não pôde ser processada.", request);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return response(401, "Unauthorized", "UNAUTHORIZED", "Credenciais inválidas.", request);
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return response(404, "Not Found", "RESOURCE_NOT_FOUND", message(ex, "Recurso não encontrado."), request);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return response(400, "Bad Request", "BAD_REQUEST", message(ex, "Requisição inválida."), request);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return response(409, "Conflict", "BUSINESS_CONFLICT",
                message(ex, "A operação não pode ser realizada no estado atual."), request);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage() == null ? "Valor inválido." : error.getDefaultMessage()));
        return response(400, "Bad Request", "VALIDATION_ERROR", "Um ou mais campos são inválidos.", request, fields);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> fields.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return response(400, "Bad Request", "VALIDATION_ERROR", "Um ou mais parâmetros são inválidos.", request, fields);
    }
    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.HttpRequestMethodNotSupportedException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(Exception ex, HttpServletRequest request) {
        return response(400, "Bad Request", "BAD_REQUEST", "A requisição é inválida ou está incompleta.", request);
    }
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalService(RestClientException ex, HttpServletRequest request) {
        return response(503, "Service Unavailable", "EXTERNAL_SERVICE_UNAVAILABLE",
                "Um serviço externo está temporariamente indisponível. Tente novamente.", request);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return response(500, "Internal Server Error", "INTERNAL_SERVER_ERROR",
                "Ocorreu um erro interno. Tente novamente.", request);
    }
    private ResponseEntity<ApiErrorResponse> response(int status, String error, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status, error, code, message, request.getRequestURI()));
    }
    private ResponseEntity<ApiErrorResponse> response(int status, String error, String code, String message, HttpServletRequest request, Map<String,String> fields) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status, error, code, message, request.getRequestURI(), fields));
    }
    private String message(Exception ex, String fallback) { return ex.getMessage() == null || ex.getMessage().isBlank() ? fallback : ex.getMessage(); }
    private String codeFor(int status) {
        return switch (status) {
            case 400 -> "BAD_REQUEST"; case 401 -> "UNAUTHORIZED"; case 403 -> "FORBIDDEN";
            case 404 -> "RESOURCE_NOT_FOUND"; case 409 -> "BUSINESS_CONFLICT";
            case 422 -> "UNPROCESSABLE_ENTITY"; case 503 -> "SERVICE_UNAVAILABLE";
            default -> status >= 500 ? "INTERNAL_SERVER_ERROR" : "HTTP_ERROR";
        };
    }
}
