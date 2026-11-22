package br.edu.ifsp.lgp2.dto;

import br.edu.ifsp.lgp2.model.ValoresChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * DTO usado no cadastro de chamado (formulário web e POST da API).
 * Ele recebe apenas os campos informados pelo usuário
 * e valida os dados com Bean Validation.
 */
public class ChamadoCadastroDTO {

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

    public ChamadoCadastroDTO() {
    }

    public ChamadoCadastroDTO(String titulo, String descricao, String categoria) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
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
}
