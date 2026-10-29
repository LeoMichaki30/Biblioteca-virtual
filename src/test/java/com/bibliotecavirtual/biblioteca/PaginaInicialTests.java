package com.bibliotecavirtual.biblioteca;

import java.nio.charset.StandardCharsets;

import com.bibliotecavirtual.biblioteca.config.SecurityConfig;
import com.bibliotecavirtual.biblioteca.config.CsrfController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CsrfController.class)
@Import(SecurityConfig.class)
class PaginaInicialTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void paginaInicialPodeSerAbertaSemLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(
                        result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        containsString("Crie sua conta para começar.")));
    }

    @Test
    void arquivosDaInterfaceSaoPublicos() throws Exception {
        for (String arquivo : new String[] { "/css/biblioteca.css", "/js/biblioteca.js", "/favicon.svg" }) {
            mockMvc.perform(get(arquivo)).andExpect(status().isOk());
        }
    }

    @Test
    void outrasRotasExigemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void postagemSemTokenCsrfContinuaBloqueada() throws Exception {
        mockMvc.perform(post("/index.html")).andExpect(status().isForbidden());
    }
}
