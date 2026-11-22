package br.edu.ifsp.lgp2.dto;

import br.edu.ifsp.lgp2.model.ValoresChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * DTO da atividade prática.
 *
 * Ele permite alterar apenas o status e a observação de atendimento.
 * Assim, a tela de atendimento não altera indevidamente título, descrição,
 * categoria, prioridade, responsável ou data de abertura.
 * O status só aceita os valores definidos em ValoresChamado.
 */
public class ChamadoAtualizacaoStatusDTO {

    private Long id;

    @NotBlank(message = "O status é obrigatório.")
    @Pattern(regexp = ValoresChamado.STATUS,
            message = "Status inválido. Use: ABERTO, EM_ANALISE, CONCLUIDO ou CANCELADO.")
    private String status;

    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
    private String observacaoAtendimento;

    public ChamadoAtualizacaoStatusDTO() {
    }

    public ChamadoAtualizacaoStatusDTO(Long id, String status, String observacaoAtendimento) {
        this.id = id;
        this.status = status;
        this.observacaoAtendimento = observacaoAtendimento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacaoAtendimento() {
        return observacaoAtendimento;
    }

    public void setObservacaoAtendimento(String observacaoAtendimento) {
        this.observacaoAtendimento = observacaoAtendimento;
    }
}
