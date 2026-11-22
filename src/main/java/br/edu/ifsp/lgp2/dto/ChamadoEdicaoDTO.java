package br.edu.ifsp.lgp2.dto;

import br.edu.ifsp.lgp2.model.ValoresChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * DTO de entrada para EDIÇÃO pela API (PUT /api/chamados/{id}).
 * Traz só os dados que podem ser alterados. Status e observação de
 * atendimento ficam de fora: têm endpoint próprio (/{id}/status).
 * O id vem da URL e a data de abertura nunca muda.
 */
public class ChamadoEdicaoDTO {

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 120, message = "O título deve ter no máximo 120 caracteres.")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    private String descricao;

    @NotBlank(message = "A categoria é obrigatória.")
    @Pattern(regexp = ValoresChamado.CATEGORIAS,
            message = "Categoria inválida. Use: Suporte técnico, Documentos, Matrícula ou Atendimento acadêmico.")
    private String categoria;

    @NotBlank(message = "A prioridade é obrigatória.")
    @Pattern(regexp = ValoresChamado.PRIORIDADES,
            message = "Prioridade inválida. Use: BAIXA, MEDIA, ALTA ou URGENTE.")
    private String prioridade;

    @NotBlank(message = "O responsável é obrigatório.")
    @Size(max = 100, message = "O responsável deve ter no máximo 100 caracteres.")
    private String responsavel;

    public ChamadoEdicaoDTO() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }
}
