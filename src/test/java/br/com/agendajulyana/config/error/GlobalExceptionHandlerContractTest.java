package br.com.agendajulyana.config.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GlobalExceptionHandlerContractTest {

    private final MockMvc mvc = standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(new LocalValidatorFactoryBean())
            .build();

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
    void shouldReturn401ForResponseStatusException() throws Exception {
        mvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldReturn400ForTypeMismatch() throws Exception {
        mvc.perform(get("/test/type-mismatch?id=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void shouldReturn409ForDataIntegrity() throws Exception {
        mvc.perform(get("/test/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_CONFLICT"));
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

    @Test
    void shouldReturn405ForUnsupportedHttpMethod() throws Exception {
        mvc.perform(post("/test/bad-request"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.path").value("/test/bad-request"));
    }

    @Test
    void shouldReturn415ForUnsupportedMediaType() throws Exception {
        mvc.perform(post("/test/media-type")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("teste"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.path").value("/test/media-type"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @GetMapping("/bad-request")
        String badRequest() {
            throw new IllegalArgumentException("inválido");
        }

        @GetMapping("/conflict")
        String conflict() {
            throw new IllegalStateException("conflito");
        }

        @GetMapping("/not-found")
        String notFound() {
            throw new EntityNotFoundException("não encontrado");
        }

        @GetMapping("/external")
        String external() {
            throw new org.springframework.web.client.RestClientException("provider down");
        }

        @GetMapping("/unauthorized")
        String unauthorized() {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "não autorizado");
        }

        @GetMapping("/type-mismatch")
        String typeMismatch(@RequestParam Integer id) {
            return id.toString();
        }

        @GetMapping("/integrity")
        String integrity() {
            throw new DataIntegrityViolationException("duplicate");
        }

        @GetMapping("/unexpected")
        String unexpected() {
            throw new RuntimeException("secret internal detail");
        }

        @PostMapping("/validation")
        String validation(@Valid @RequestBody Request request) {
            return request.name();
        }

        @PostMapping(value = "/media-type", consumes = MediaType.APPLICATION_JSON_VALUE)
        String mediaType(@RequestBody Request request) {
            return request.name();
        }
    }

    record Request(@NotBlank(message = "Nome é obrigatório.") String name) {}
}
