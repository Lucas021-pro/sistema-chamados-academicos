package br.edu.ifsp.lgp2.factory;

import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.model.Chamado;

import java.time.LocalDate;

/*
- Factory centraliza a criação e a configuração inicial de chamados novos,
- definindo status inicial, datas, prioridade, responsável e histórico.
 */
public final class ChamadoFactory {

    private ChamadoFactory() {
    }

    public static Chamado criar(ChamadoCadastroDTO dto) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        chamado.setCategoria(dto.getCategoria());

        LocalDate hoje = LocalDate.now();
        chamado.setStatus("ABERTO");
        chamado.setDataAbertura(hoje);
        chamado.setDataAtualizacao(hoje);

        switch (dto.getCategoria()) {
            case "Suporte técnico" -> {
                chamado.setPrioridade("ALTA");
                chamado.setResponsavel("Equipe de TI");
            }
            case "Documentos" -> {
                chamado.setPrioridade("MEDIA");
                chamado.setResponsavel("Secretaria Acadêmica");
            }
            case "Matrícula" -> {
                chamado.setPrioridade("ALTA");
                chamado.setResponsavel("Coordenação de Curso");
            }
            default -> {
                chamado.setPrioridade("BAIXA");
                chamado.setResponsavel("Atendimento Geral");
            }
        }

        chamado.registrarHistorico(
                chamado.getStatus(),
                chamado.getResponsavel(),
                "Chamado criado pela pessoa solicitante."
        );

        return chamado;
    }
}