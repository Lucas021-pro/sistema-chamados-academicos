package br.edu.ifsp.lgp2.exception;

import br.edu.ifsp.lgp2.controller.ChamadoApiController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Transforma erros de entrada da API em respostas 400 com corpo JSON,
 * em vez de deixar o servidor responder 500.
 * Vale somente para o ChamadoApiController (as telas web não são afetadas).
 */
@RestControllerAdvice(assignableTypes = ChamadoApiController.class)
public class ApiExceptionHandler {

    /* Falha de validação (@Valid no corpo) ou de conversão dos filtros da URL. */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErroApiResponse> tratarValidacao(BindException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", campos);
    }

    /* JSON ausente, malformado ou com tipo incompatível. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApiResponse> tratarCorpoIlegivel(HttpMessageNotReadableException ex) {
        return montarResposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente ou JSON malformado.", null);
    }

    /* Ex.: /api/chamados/abc, onde o id deveria ser numérico. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroApiResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return montarResposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'.", null);
    }

    private ResponseEntity<ErroApiResponse> montarResposta(HttpStatus status, String mensagem,
                                                           Map<String, String> campos) {
        ErroApiResponse corpo = new ErroApiResponse(
                status.value(), status.getReasonPhrase(), mensagem, campos, LocalDateTime.now());
        return ResponseEntity.status(status).body(corpo);
    }
}
