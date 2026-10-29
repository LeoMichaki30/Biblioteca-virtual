package com.bibliotecavirtual.biblioteca.usuario;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
        @NotBlank(message = "Informe seu nome.")
        @Size(max = 100, message = "O nome deve ter até 100 caracteres.") String nome,
        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 254, message = "O e-mail deve ter até 254 caracteres.") String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "Informe uma senha.")
        @Size(min = 8, max = 128, message = "A senha deve ter entre 8 e 128 caracteres.") String senha) {

    // Normaliza antes da validação; a senha é preservada exatamente como recebida.
    public CadastroUsuarioRequest {
        nome = nome == null ? null : nome.strip();
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "CadastroUsuarioRequest[conteudo protegido]";
    }
}
