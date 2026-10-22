package br.com.agendajulyana.config.error;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import java.io.IOException;

public final class ApiSecurityErrorWriter {
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private ApiSecurityErrorWriter() {}

    public static void write(HttpServletRequest request, HttpServletResponse response,
                             int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        JSON_MAPPER.writeValue(response.getWriter(), new ApiErrorResponse(
                status,
                status == 401 ? "Unauthorized" : "Forbidden",
                code,
                message,
                request.getRequestURI()));
    }
}
