package br.edu.ifsp.lgp2.service;

import br.edu.ifsp.lgp2.dto.ChamadoAtualizacaoStatusDTO;
import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.dto.ChamadoEdicaoDTO;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void deveSalvarNovoChamadoComHistoricoInicial() {
        ChamadoCadastroDTO dto = new ChamadoCadastroDTO(
                "Wi-Fi instável",
                "Queda frequente de conexão na biblioteca.",
                "Suporte técnico"
        );
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        Chamado salvo = chamadoService.salvarNovoChamado(dto);

        assertEquals("ABERTO", salvo.getStatus());
        assertEquals("ALTA", salvo.getPrioridade());
        assertEquals(1, salvo.getHistorico().size());
        verify(chamadoRepository).save(any(Chamado.class));
    }

    @Test
    void deveAtualizarDadosPreservandoStatusEDataAberturaERegistrandoHistorico() {
        LocalDate aberturaOriginal = LocalDate.now().minusDays(5);
        Chamado existente = new Chamado();
        existente.setId(10L);
        existente.setTitulo("Título antigo");
        existente.setDescricao("Descrição antiga");
        existente.setCategoria("Documentos");
        existente.setStatus("EM_ANALISE");
        existente.setPrioridade("MEDIA");
        existente.setResponsavel("Secretaria Acadêmica");
        existente.setDataAbertura(aberturaOriginal);
        existente.registrarHistorico("ABERTO", "Secretaria Acadêmica", "Chamado criado.");

        ChamadoEdicaoDTO edicao = new ChamadoEdicaoDTO();
        edicao.setTitulo("Título corrigido");
        edicao.setDescricao("Descrição detalhada atualizada");
        edicao.setCategoria("Matrícula");
        edicao.setPrioridade("URGENTE");
        edicao.setResponsavel("Coordenação de Curso");

        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Chamado> resultado = chamadoService.atualizarDados(10L, edicao);

        assertTrue(resultado.isPresent());
        Chamado atualizado = resultado.get();
        assertEquals("Título corrigido", atualizado.getTitulo());
        assertEquals("URGENTE", atualizado.getPrioridade());
        assertEquals("EM_ANALISE", atualizado.getStatus());
        assertEquals(aberturaOriginal, atualizado.getDataAbertura());
        assertEquals(2, atualizado.getHistorico().size());
        assertEquals("Dados da solicitação atualizados.", atualizado.getHistorico().get(1).getObservacao());
    }

    @Test
    void deveRetornarVazioAoAtualizarDadosDeChamadoInexistente() {
        when(chamadoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Chamado> resultado = chamadoService.atualizarDados(999L, new ChamadoEdicaoDTO());

        assertTrue(resultado.isEmpty());
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarStatusERegistrarHistorico() {
        Chamado existente = new Chamado();
        existente.setId(4L);
        existente.setStatus("ABERTO");
        existente.setResponsavel("Equipe de TI");

        ChamadoAtualizacaoStatusDTO dto = new ChamadoAtualizacaoStatusDTO();
        dto.setId(4L);
        dto.setStatus("CONCLUIDO");
        dto.setObservacaoAtendimento("Cabo substituído e equipamento testado.");

        when(chamadoRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        Chamado atualizado = chamadoService.atualizarStatus(dto);

        assertNotNull(atualizado);
        assertEquals("CONCLUIDO", atualizado.getStatus());
        assertEquals("Cabo substituído e equipamento testado.", atualizado.getObservacaoAtendimento());
        assertEquals(1, atualizado.getHistorico().size());
        assertEquals("CONCLUIDO", atualizado.getHistorico().get(0).getStatus());
    }
}