package com.bibliotecavirtual.biblioteca.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.bibliotecavirtual.biblioteca.usuario.EmailJaCadastradoException;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> dadosInvalidos(MethodArgumentNotValidException exception) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(erro ->
                campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResponse("Confira os campos informados.", campos));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> emailDuplicado(EmailJaCadastradoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse(exception.getMessage(), Map.of("email", exception.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido() {
        return ResponseEntity.badRequest().body(new ErroResponse("Não foi possível ler os dados enviados.", Map.of()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErroResponse> falhaNoBanco() {
        // Não devolve SQL, credenciais ou detalhes internos ao navegador.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse("Não foi possível concluir o cadastro agora. Tente novamente.", Map.of()));
    }

    public record ErroResponse(String mensagem, Map<String, String> campos) {
    }
}
