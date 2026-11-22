package br.edu.ifsp.lgp2.factory;

import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.model.Chamado;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ChamadoFactoryTest {

    @Test
    void deveCriarChamadoDeSuporteTecnicoComPrioridadeAltaEEquipeTi() {
        ChamadoCadastroDTO dto = new ChamadoCadastroDTO(
                "Computador não liga",
                "Máquina da bancada 3 do laboratório 2 não inicia.",
                "Suporte técnico"
        );

        Chamado chamado = ChamadoFactory.criar(dto);

        assertEquals("ABERTO", chamado.getStatus());
        assertEquals("ALTA", chamado.getPrioridade());
        assertEquals("Equipe de TI", chamado.getResponsavel());
        assertEquals(LocalDate.now(), chamado.getDataAbertura());
        assertEquals(LocalDate.now(), chamado.getDataAtualizacao());
        assertEquals(1, chamado.getHistorico().size());
        assertEquals("ABERTO", chamado.getHistorico().get(0).getStatus());
    }

    @Test
    void deveDefinirPrioridadeEResponsavelConformeCategoria() {
        Chamado documentos = ChamadoFactory.criar(
                new ChamadoCadastroDTO("Declaração", "Solicito declaração de matrícula.", "Documentos")
        );
        Chamado matricula = ChamadoFactory.criar(
                new ChamadoCadastroDTO("Ajuste", "Inclusão em disciplina.", "Matrícula")
        );
        Chamado geral = ChamadoFactory.criar(
                new ChamadoCadastroDTO("Dúvida", "Informações sobre monitoria.", "Atendimento acadêmico")
        );

        assertEquals("MEDIA", documentos.getPrioridade());
        assertEquals("Secretaria Acadêmica", documentos.getResponsavel());

        assertEquals("ALTA", matricula.getPrioridade());
        assertEquals("Coordenação de Curso", matricula.getResponsavel());

        assertEquals("BAIXA", geral.getPrioridade());
        assertEquals("Atendimento Geral", geral.getResponsavel());
    }
}