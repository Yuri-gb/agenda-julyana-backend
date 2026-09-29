package br.com.agendajulyana.auth.security;

import br.com.agendajulyana.auth.controller.AuthController;
import br.com.agendajulyana.auth.dto.MeResponse;
import br.com.agendajulyana.auth.service.AuthService;
import br.com.agendajulyana.config.error.ApiAccessDeniedHandler;
import br.com.agendajulyana.config.error.ApiAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private GoogleAuthenticationSuccessHandler googleAuthenticationSuccessHandler;

    @MockBean
    private ApiAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private ApiAccessDeniedHandler accessDeniedHandler;

    @Test
    void endpointMeDeveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teste@example.com")
    void endpointMeDevePermitirUsuarioAutenticado() throws Exception {
        var response = new MeResponse(
                UUID.randomUUID(),
                "Teste",
                "teste@example.com",
                null,
                "ATIVO",
                Set.of("CLIENTE"),
                false
        );

        when(authService.me("teste@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/auth/me").with(user("teste@example.com")))
                .andExpect(status().isOk());
    }
}
