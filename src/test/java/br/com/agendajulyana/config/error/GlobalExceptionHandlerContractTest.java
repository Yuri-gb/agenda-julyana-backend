package br.com.agendajulyana.config.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerContractTest.TestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerContractTest {

    @Autowired MockMvc mvc;

    @Test
    void shouldReturn400ForIllegalArgument() throws Exception {
        mvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/test/bad-request"));
    }

    @Test
    void shouldReturn409ForIllegalState() throws Exception {
        mvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("BUSINESS_CONFLICT"));
    }

    @Test
    void shouldReturn404ForEntityNotFound() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturn503ForExternalService() throws Exception {
        mvc.perform(get("/test/external"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.code").value("EXTERNAL_SERVICE_UNAVAILABLE"));
    }

    @Test
    void shouldReturn500ForUnexpectedException() throws Exception {
        mvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno. Tente novamente."));
    }

    @Test
    void shouldReturn400ForValidation() throws Exception {
        mvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @GetMapping("/bad-request") String badRequest() { throw new IllegalArgumentException("inválido"); }
        @GetMapping("/conflict") String conflict() { throw new IllegalStateException("conflito"); }
        @GetMapping("/not-found") String notFound() { throw new jakarta.persistence.EntityNotFoundException("não encontrado"); }
        @GetMapping("/external") String external() { throw new org.springframework.web.client.RestClientException("provider down"); }
        @GetMapping("/unexpected") String unexpected() { throw new RuntimeException("secret internal detail"); }
        @PostMapping("/validation") String validation(@Valid @RequestBody Request request) { return request.name(); }
    }

    record Request(@NotBlank(message = "Nome é obrigatório.") String name) {}
}
