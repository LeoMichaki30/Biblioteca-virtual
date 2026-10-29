package com.bibliotecavirtual.biblioteca.usuario;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroUsuarioRequest dados) {
        if (repository.existsByEmail(dados.email())) {
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = new Usuario(dados.nome(), dados.email(), passwordEncoder.encode(dados.senha()));
        try {
            return UsuarioResponse.de(repository.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException exception) {
            // A restrição do banco também protege contra cadastros simultâneos.
            for (Throwable causa = exception; causa != null; causa = causa.getCause()) {
                if (causa instanceof ConstraintViolationException constraint
                        && "uk_usuarios_email".equals(constraint.getConstraintName())) {
                    throw new EmailJaCadastradoException();
                }
            }
            throw exception;
        }
    }
}
