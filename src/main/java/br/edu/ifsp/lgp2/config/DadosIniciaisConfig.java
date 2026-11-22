package br.edu.ifsp.lgp2.config;

import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class DadosIniciaisConfig {

    @Bean
    CommandLineRunner carregarDadosIniciais(ChamadoRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            LocalDate hoje = LocalDate.now();

            List<Chamado> chamados = List.of(
                    montar("Computador 05 do laboratório de redes não liga",
                            "A máquina da bancada 5 não apresenta sinal de energia ao pressionar o botão frontal.",
                            "Suporte técnico", "ALTA", "Equipe de TI", "ABERTO", hoje, null),

                    montar("Declaração de matrícula para passe escolar",
                            "Solicito declaração de matrícula atualizada para renovação do bilhete estudantil.",
                            "Documentos", "MEDIA", "Secretaria Acadêmica", "CONCLUIDO", hoje.minusDays(1),
                            "Declaração emitida e disponibilizada no portal do estudante."),

                    montar("Inclusão de disciplina optativa no semestre",
                            "Solicito orientação e ajuste para incluir disciplina optativa dentro do prazo.",
                            "Matrícula", "MEDIA", "Coordenação de Curso", "EM_ANALISE", hoje.minusDays(2),
                            "Verificando disponibilidade de vagas na turma solicitada."),

                    montar("Dúvida sobre aproveitamento de estudos anteriores",
                            "Gostaria de saber o procedimento e prazos para solicitar dispensa de disciplina.",
                            "Atendimento acadêmico", "BAIXA", "Atendimento Acadêmico", "CONCLUIDO", hoje.minusDays(4),
                            "Orientações e formulário enviados por e-mail institucional."),

                    montar("Sem acesso ao sistema acadêmico com senha antiga",
                            "Após redefinir a senha, o login continua falhando no portal do estudante.",
                            "Suporte técnico", "ALTA", "Equipe de TI", "EM_ANALISE", hoje.minusDays(5),
                            "Sincronização de credenciais em andamento junto ao diretório."),

                    montar("Histórico escolar parcial para estágio obrigatório",
                            "Preciso do histórico escolar parcial assinado para entrega na empresa concedente.",
                            "Documentos", "ALTA", "Secretaria Acadêmica", "CONCLUIDO", hoje.minusDays(8),
                            "Histórico assinado digitalmente anexado ao protocolo."),

                    montar("Pendência de comprovante na efetivação de matrícula",
                            "Recebi aviso de pendência documental e preciso confirmar a situação da matrícula.",
                            "Matrícula", "URGENTE", "Coordenação de Curso", "ABERTO", hoje.minusDays(10), null),

                    montar("Impressora da secretaria com atolamento de papel",
                            "O equipamento exibe mensagem de papel preso na bandeja inferior e interrompeu a fila.",
                            "Suporte técnico", "MEDIA", "Equipe de TI", "CONCLUIDO", hoje.minusDays(12),
                            "Papel removido, roletes limpos e impressão normalizada."),

                    montar("Horário de atendimento da coordenação do curso",
                            "Gostaria de confirmar os dias e horários de atendimento presencial da coordenação.",
                            "Atendimento acadêmico", "BAIXA", "Atendimento Acadêmico", "CONCLUIDO", hoje.minusDays(15),
                            "Atendimento presencial às terças e quintas, das 18h às 21h."),

                    montar("Atestado de frequência para bolsa estudantil",
                            "Solicito documento comprobatório de frequência referente ao mês vigente.",
                            "Documentos", "MEDIA", "Secretaria Acadêmica", "EM_ANALISE", hoje.minusDays(18),
                            "Aguardando fechamento do diário pelo docente responsável."),

                    montar("Solicitação de trancamento de matrícula",
                            "Preciso de informações sobre regras e prazo para trancar o curso neste semestre.",
                            "Matrícula", "BAIXA", "Coordenação de Curso", "CANCELADO", hoje.minusDays(22),
                            "Cancelado a pedido do próprio aluno após orientação."),

                    montar("Projetor da sala 108 sem imagem no HDMI",
                            "O projetor liga normalmente, mas não detecta o sinal de vídeo enviado pelo notebook.",
                            "Suporte técnico", "ALTA", "Equipe de TI", "CONCLUIDO", hoje.minusDays(26),
                            "Cabo HDMI substituído e projeção testada."),

                    montar("Regras para horas complementares do curso",
                            "Dúvida sobre quais certificados e atividades são aceitos como horas complementares.",
                            "Atendimento acadêmico", "BAIXA", "Atendimento Acadêmico", "CONCLUIDO", hoje.minusDays(35),
                            "Tabela de atividades e limites por categoria enviada."),

                    montar("Ementa de disciplina para transferência externa",
                            "Solicito a ementa oficial das disciplinas cursadas para processo de transferência.",
                            "Documentos", "MEDIA", "Secretaria Acadêmica", "ABERTO", hoje.minusDays(42), null),

                    montar("Troca de turma por choque de horário",
                            "Solicito alteração de turma devido a conflito de horário com disciplina obrigatória.",
                            "Matrícula", "ALTA", "Coordenação de Curso", "CONCLUIDO", hoje.minusDays(50),
                            "Remanejamento efetuado conforme grade horária.")
            );

            repository.saveAll(chamados);
        };
    }

    private Chamado montar(String titulo,
                           String descricao,
                           String categoria,
                           String prioridade,
                           String responsavel,
                           String status,
                           LocalDate dataAbertura,
                           String observacao) {
        Chamado c = new Chamado();
        c.setTitulo(titulo);
        c.setDescricao(descricao);
        c.setCategoria(categoria);
        c.setPrioridade(prioridade);
        c.setResponsavel(responsavel);
        c.setStatus(status);
        c.setDataAbertura(dataAbertura);
        c.setDataAtualizacao(dataAbertura);
        c.setObservacaoAtendimento(observacao);

        LocalDateTime horarioAbertura = LocalDateTime.of(dataAbertura, LocalTime.of(9, 0));
        c.registrarHistorico(horarioAbertura, "ABERTO", responsavel, "Chamado aberto no sistema.");

        if (!"ABERTO".equals(status)) {
            c.registrarHistorico(horarioAbertura.plusHours(2), status, responsavel, observacao);
        }

        return c;
    }
}