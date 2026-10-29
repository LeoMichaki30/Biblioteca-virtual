package com.bibliotecavirtual.biblioteca.usuario;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastroIntegracaoTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository repository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void cadastraNoPostgresComTokenRealNormalizaEmailEProtegeSenha() throws Exception {
        String email = "teste-" + UUID.randomUUID() + "@example.com";
        var tokenResult = mockMvc.perform(get("/api/csrf")).andExpect(status().isOk()).andReturn();
        JsonNode token = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        MockHttpSession session = (MockHttpSession) tokenResult.getRequest().getSession(false);
        var dados = new CadastroUsuarioRequest("  Leitora de teste  ", "  " + email.toUpperCase(java.util.Locale.ROOT) + "  ", "ficticia-123");
        // O DTO aceita a senha apenas na entrada JSON; o corpo abaixo simula o navegador.
        String corpo = objectMapper.writeValueAsString(java.util.Map.of("nome", "  Leitora de teste  ",
                "email", "  " + email.toUpperCase(java.util.Locale.ROOT) + "  ", "senha", "ficticia-123"));
        mockMvc.perform(post("/api/usuarios").session(session)
                        .header(token.get("headerName").asText(), token.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Leitora de teste"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        Usuario salvo = repository.findByEmail(email).orElseThrow();
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getSenhaHash()).isNotEqualTo(dados.senha());
        assertThat(passwordEncoder.matches(dados.senha(), salvo.getSenhaHash())).isTrue();
        assertThat(passwordEncoder.matches("senha-incorreta", salvo.getSenhaHash())).isFalse();

        mockMvc.perform(post("/api/usuarios").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
        // A transação do teste é desfeita; essa conta fictícia não permanece no banco.
    }
}
