package br.edu.ifsp.lgp2.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
- Entity representa a tabela CHAMADO no banco de dados.
- Ela possui todos os campos persistidos.
 */
@Entity
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, length = 80)
    private String categoria;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, length = 30)
    private String prioridade;

    @Column(nullable = false, length = 100)
    private String responsavel;

    private LocalDate dataAbertura;

    private LocalDate dataAtualizacao;

    @Column(length = 500)
    private String observacaoAtendimento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chamado_historico", joinColumns = @JoinColumn(name = "chamado_id"))
    @OrderColumn(name = "ordem")
    private List<HistoricoAlteracao> historico = new ArrayList<>();

    public Chamado() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDate dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDate getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDate dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getObservacaoAtendimento() {
        return observacaoAtendimento;
    }

    public void setObservacaoAtendimento(String observacaoAtendimento) {
        this.observacaoAtendimento = observacaoAtendimento;
    }

    public List<HistoricoAlteracao> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoAlteracao> historico) {
        this.historico = historico;
    }

    public void registrarHistorico(String status, String responsavel, String observacao) {
        registrarHistorico(LocalDateTime.now(), status, responsavel, observacao);
    }

    public void registrarHistorico(LocalDateTime dataHora, String status, String responsavel, String observacao) {
        if (this.historico == null) {
            this.historico = new ArrayList<>();
        }
        this.historico.add(new HistoricoAlteracao(dataHora, status, responsavel, observacao));
    }

    @Embeddable
    public static class HistoricoAlteracao {

        private LocalDateTime dataHora;
        private String status;
        private String responsavel;

        @Column(length = 500)
        private String observacao;

        public HistoricoAlteracao() {
        }

        public HistoricoAlteracao(LocalDateTime dataHora, String status, String responsavel, String observacao) {
            this.dataHora = dataHora;
            this.status = status;
            this.responsavel = responsavel;
            this.observacao = observacao;
        }

        public LocalDateTime getDataHora() {
            return dataHora;
        }

        public void setDataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getResponsavel() {
            return responsavel;
        }

        public void setResponsavel(String responsavel) {
            this.responsavel = responsavel;
        }

        public String getObservacao() {
            return observacao;
        }

        public void setObservacao(String observacao) {
            this.observacao = observacao;
        }
    }
}