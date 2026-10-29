package com.bibliotecavirtual.biblioteca.usuario;

import java.util.UUID;

// A resposta pública nunca contém senha nem hash.
public record UsuarioResponse(UUID id, String nome, String email) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
