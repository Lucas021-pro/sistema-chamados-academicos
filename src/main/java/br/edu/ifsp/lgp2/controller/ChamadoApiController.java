package br.edu.ifsp.lgp2.controller;

import br.edu.ifsp.lgp2.dto.ChamadoAtualizacaoStatusDTO;
import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.dto.ChamadoEdicaoDTO;
import br.edu.ifsp.lgp2.dto.FiltroChamadoDTO;
import br.edu.ifsp.lgp2.dto.IndicadoresDTO;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.service.ChamadoService;
import br.edu.ifsp.lgp2.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/*
- API REST: responde JSON em vez de telas HTML; a mesma camada Service é reutilizada.
- Entradas inválidas viram 400 (ApiExceptionHandler); chamado inexistente vira 404.
 */
@RestController
@RequestMapping("/api/chamados")
public class ChamadoApiController {

    private final ChamadoService chamadoService;
    private final ConsultaService consultaService;

    public ChamadoApiController(ChamadoService chamadoService, ConsultaService consultaService) {
        this.chamadoService = chamadoService;
        this.consultaService = consultaService;
    }

    @GetMapping
    public List<Chamado> listar(@ModelAttribute FiltroChamadoDTO filtro) {
        return consultaService.consultar(filtro);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chamado> buscar(@PathVariable Long id) {
        return ResponseEntity.of(chamadoService.buscarOpcional(id));
    }

    @GetMapping("/indicadores")
    public IndicadoresDTO indicadores() {
        return consultaService.indicadores();
    }

    @PostMapping
    public ResponseEntity<Chamado> criar(@Valid @RequestBody ChamadoCadastroDTO dto) {
        Chamado criado = chamadoService.salvarNovoChamado(dto);

        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();

        return ResponseEntity.created(localizacao).body(criado);
    }

    /* Atualiza os dados do chamado (menos status e observação, que têm endpoint próprio). */
    @PutMapping("/{id}")
    public ResponseEntity<Chamado> atualizar(@PathVariable Long id,
                                             @Valid @RequestBody ChamadoEdicaoDTO dto) {
        return ResponseEntity.of(chamadoService.atualizarDados(id, dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Chamado> atualizarStatus(@PathVariable Long id,
                                                   @Valid @RequestBody ChamadoAtualizacaoStatusDTO dto) {
        dto.setId(id);
        return ResponseEntity.of(Optional.ofNullable(chamadoService.atualizarStatus(dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!chamadoService.existe(id)) {
            return ResponseEntity.notFound().build();
        }
        chamadoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}