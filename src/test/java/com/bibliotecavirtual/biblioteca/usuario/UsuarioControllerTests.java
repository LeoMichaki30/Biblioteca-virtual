package com.bibliotecavirtual.biblioteca.usuario;

import java.util.UUID;

import com.bibliotecavirtual.biblioteca.config.CsrfController;
import com.bibliotecavirtual.biblioteca.config.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { UsuarioController.class, CsrfController.class })
@Import(SecurityConfig.class)
class UsuarioControllerTests {
    private static final String CADASTRO = """
            {"nome":"Leitora","email":"leitora@example.com","senha":"ficticia-123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @Test
    void cadastroRetorna201SemSenhaNemHash() throws Exception {
        when(service.cadastrar(any())).thenReturn(new UsuarioResponse(UUID.randomUUID(), "Leitora", "leitora@example.com"));
        mockMvc.perform(post("/api/usuarios").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("leitora@example.com"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void dadosInvalidosNaoChegamAoServico() throws Exception {
        mockMvc.perform(post("/api/usuarios").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"  ","email":"invalido","senha":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").isNotEmpty())
                .andExpect(jsonPath("$.campos.email").isNotEmpty())
                .andExpect(jsonPath("$.campos.senha").isNotEmpty())
                .andExpect(jsonPath("$.rejectedValue").doesNotExist());
        verifyNoInteractions(service);
    }

    @Test
    void emailExistenteRetorna409() throws Exception {
        when(service.cadastrar(any())).thenThrow(new EmailJaCadastradoException());
        mockMvc.perform(post("/api/usuarios").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.email").value("Já existe uma conta com esse e-mail."));
    }

    @Test
    void cadastroSemCsrfContinuaBloqueado() throws Exception {
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void tokenCsrfPodeSerObtidoSemLoginENaoDeveSerCacheado() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void erroDoBancoNaoExpoeDetalhesInternos() throws Exception {
        when(service.cadastrar(any())).thenThrow(new DataIntegrityViolationException("SQL interno ficticio"));
        mockMvc.perform(post("/api/usuarios").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Não foi possível concluir o cadastro agora. Tente novamente."));
    }
}
