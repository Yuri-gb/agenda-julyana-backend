package br.com.agendajulyana.config.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        OffsetDateTime timestamp, int status, String error, String code,
        String message, String path, Map<String, String> fieldErrors) {
    public ApiErrorResponse(int status, String error, String code, String message, String path) {
        this(OffsetDateTime.now(), status, error, code, message, path, Map.of());
    }
    public ApiErrorResponse(int status, String error, String code, String message,
                            String path, Map<String, String> fieldErrors) {
        this(OffsetDateTime.now(), status, error, code, message, path,
                fieldErrors == null ? Map.of() : fieldErrors);
    }
}
