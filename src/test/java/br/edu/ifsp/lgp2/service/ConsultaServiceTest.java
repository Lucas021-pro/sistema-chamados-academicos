package br.edu.ifsp.lgp2.service;

import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.dto.FiltroChamadoDTO;
import br.edu.ifsp.lgp2.dto.IndicadoresDTO;
import br.edu.ifsp.lgp2.factory.ChamadoFactory;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ConsultaService.class)
class ConsultaServiceTest {

    @Autowired
    private ChamadoRepository repository;

    @Autowired
    private ConsultaService consultaService;

    private Chamado c1;
    private Chamado c2;

    @BeforeEach
    void prepararDados() {
        repository.deleteAll();

        c1 = ChamadoFactory.criar(new ChamadoCadastroDTO(
                "Projetor sem imagem",
                "Sala 204 sem sinal HDMI.",
                "Suporte técnico"
        ));
        c1.setDataAbertura(LocalDate.now().minusDays(2));
        c1 = repository.save(c1);

        c2 = ChamadoFactory.criar(new ChamadoCadastroDTO(
                "Histórico escolar",
                "Solicitação de histórico atualizado.",
                "Documentos"
        ));
        c2.setStatus("CONCLUIDO");
        c2.setDataAbertura(LocalDate.now().minusDays(20));
        c2 = repository.save(c2);
    }

    @Test
    void deveBuscarExatamentePorHashId() {
        FiltroChamadoDTO filtro = new FiltroChamadoDTO();
        filtro.setBusca("#" + c1.getId());

        List<Chamado> resultado = consultaService.consultar(filtro);

        assertEquals(1, resultado.size());
        assertEquals(c1.getId(), resultado.get(0).getId());
    }

    @Test
    void deveFiltrarPorPeriodoECalcularIndicadoresDoRecorte() {
        FiltroChamadoDTO ultimos7Dias = new FiltroChamadoDTO();
        ultimos7Dias.setInicio(LocalDate.now().minusDays(7));
        ultimos7Dias.setFim(LocalDate.now());

        IndicadoresDTO indicadores = consultaService.indicadores(ultimos7Dias);

        assertEquals(1, indicadores.total());
        assertEquals(1, indicadores.abertos());
        assertEquals(0, indicadores.concluidos());
        assertEquals(1, indicadores.altaPrioridade());
        assertEquals(0.0, indicadores.percentualConcluido());
    }
}