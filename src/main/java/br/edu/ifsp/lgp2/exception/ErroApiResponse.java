package br.edu.ifsp.lgp2.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/*
 * Corpo JSON padrão dos erros da API.
 * O campo "campos" só aparece quando há erros de validação por campo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroApiResponse(
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos,
        LocalDateTime timestamp
) {
}
