package com.bibliotecavirtual.biblioteca.usuario;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios", schema = "app")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @JsonIgnore
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    // O JPA precisa de um construtor sem argumentos para carregar usuarios.
    protected Usuario() {
    }

    // Recebe o hash ja calculado; nao deve receber a senha original.
    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getSenhaHash() {
        return senhaHash;
    }
}
