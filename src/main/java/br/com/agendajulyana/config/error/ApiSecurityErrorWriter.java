package br.com.agendajulyana.config.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import java.io.IOException;

public final class ApiSecurityErrorWriter {
    private ApiSecurityErrorWriter() {}
    public static void write(ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response,
                             int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), new ApiErrorResponse(status,
                status == 401 ? "Unauthorized" : "Forbidden", code, message, request.getRequestURI()));
    }
}
