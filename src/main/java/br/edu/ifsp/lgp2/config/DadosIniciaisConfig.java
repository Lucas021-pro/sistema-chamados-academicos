package br.edu.ifsp.lgp2.config;

import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.factory.ChamadoFactory;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

/*
- Carga inicial para demonstração: só roda com o banco vazio.
 */
@Configuration
public class DadosIniciaisConfig {

    private record ChamadoDemo(String titulo, String descricao, String categoria, String status,
                               String prioridade, String observacao, int diasAtras) {
    }

    @Bean
    public CommandLineRunner carregaDadosIniciais(ChamadoRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            List<ChamadoDemo> exemplos = List.of(
                    new ChamadoDemo("Problema no laboratório", "Computador do laboratório não liga.",
                            "Suporte técnico", "ABERTO", null, null, 1),
                    new ChamadoDemo("Solicitação de declaração", "Preciso de declaração de matrícula.",
                            "Documentos", "ABERTO", null, null, 2),
                    new ChamadoDemo("Ajuste de matrícula", "Solicito ajuste em unidade curricular.",
                            "Matrícula", "EM_ANALISE", null, "Encaminhado à coordenação para análise.", 4),
                    new ChamadoDemo("Wi-Fi instável na biblioteca", "A rede cai várias vezes durante o dia.",
                            "Suporte técnico", "EM_ANALISE", "URGENTE",
                            "Equipe de TI verificando o roteador do 2º andar.", 6),
                    new ChamadoDemo("Histórico escolar", "Preciso do histórico escolar atualizado.",
                            "Documentos", "CONCLUIDO", null, "Documento enviado por e-mail.", 12),
                    new ChamadoDemo("Dúvida sobre estágio",
                            "Quais documentos são necessários para o estágio obrigatório?",
                            "Atendimento acadêmico", "CONCLUIDO", null, "Orientações enviadas ao aluno.", 18),
                    new ChamadoDemo("Cancelamento de matrícula em disciplina", "Solicitação feita por engano.",
                            "Matrícula", "CANCELADO", null, "Solicitação cancelada a pedido do aluno.", 25),
                    new ChamadoDemo("Projetor da sala 204 sem imagem", "O projetor liga, mas não exibe imagem.",
                            "Suporte técnico", "CONCLUIDO", null, "Cabo HDMI substituído.", 30),
                    new ChamadoDemo("Declaração de vínculo para estágio",
                            "Preciso de declaração de vínculo com a instituição.",
                            "Documentos", "CONCLUIDO", null, "Declaração emitida pela secretaria.", 38),
                    new ChamadoDemo("Trancamento de matrícula",
                            "Quero saber o prazo e o procedimento para trancar o semestre.",
                            "Matrícula", "ABERTO", null, null, 45),
                    new ChamadoDemo("Acesso ao e-mail institucional", "Esqueci a senha do e-mail institucional.",
                            "Suporte técnico", "CONCLUIDO", "MEDIA", "Senha redefinida pela equipe de TI.", 52),
                    new ChamadoDemo("Informações sobre monitoria",
                            "Como me candidato à monitoria no próximo semestre?",
                            "Atendimento acadêmico", "EM_ANALISE", null, "Aguardando edital da coordenação.", 58)
            );

            repository.saveAll(exemplos.stream().map(this::montarChamado).toList());
        };
    }

    private Chamado montarChamado(ChamadoDemo exemplo) {
        ChamadoCadastroDTO dto = new ChamadoCadastroDTO(
                exemplo.titulo(),
                exemplo.descricao(),
                exemplo.categoria()
        );
        Chamado chamado = ChamadoFactory.criar(dto);

        LocalDate abertura = LocalDate.now().minusDays(exemplo.diasAtras());
        LocalDate atualizacao = abertura.plusDays(Math.min(exemplo.diasAtras(), 2));

        chamado.setDataAbertura(abertura);
        chamado.setDataAtualizacao(atualizacao);

        // Ajusta a data do evento inicial criado pela Factory para a data simulada de abertura
        if (!chamado.getHistorico().isEmpty()) {
            chamado.getHistorico().get(0).setDataHora(abertura.atTime(9, 15));
        }

        if (exemplo.prioridade() != null) {
            chamado.setPrioridade(exemplo.prioridade());
        }

        chamado.setStatus(exemplo.status());
        chamado.setObservacaoAtendimento(exemplo.observacao());

        if (!"ABERTO".equals(exemplo.status())) {
            chamado.registrarHistorico(
                    atualizacao.atTime(14, 30),
                    exemplo.status(),
                    chamado.getResponsavel(),
                    exemplo.observacao()
            );
        }

        return chamado;
    }
}