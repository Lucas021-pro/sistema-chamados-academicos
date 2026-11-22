package br.edu.ifsp.lgp2.controller;

import br.edu.ifsp.lgp2.dto.FiltroChamadoDTO;
import br.edu.ifsp.lgp2.service.ConsultaService;
import br.edu.ifsp.lgp2.service.ExportacaoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

/**
 - Controller MVC: apresenta indicadores, relatórios filtrados e devolve arquivos de exportação.
 */
@Controller
public class RelatorioController {

    private final ConsultaService consultaService;
    private final ExportacaoService exportacaoService;

    public RelatorioController(ConsultaService consultaService, ExportacaoService exportacaoService) {
        this.consultaService = consultaService;
        this.exportacaoService = exportacaoService;
    }

    @GetMapping("/indicadores")
    public String indicadores(@ModelAttribute("filtro") FiltroChamadoDTO filtro, Model model) {
        model.addAttribute("filtro", filtro);
        model.addAttribute("indicadores", consultaService.indicadores(filtro));
        return "indicadores";
    }

    @GetMapping("/relatorios")
    public String relatorios(@ModelAttribute("filtro") FiltroChamadoDTO filtro, Model model) {
        var registros = consultaService.consultar(filtro);
        model.addAttribute("filtro", filtro);
        model.addAttribute("chamados", registros);
        model.addAttribute("indicadores", consultaService.indicadores(filtro));
        return "relatorios";
    }

    @GetMapping("/relatorios/exportar/{formato}")
    public ResponseEntity<byte[]> exportar(@PathVariable String formato,
                                           @ModelAttribute FiltroChamadoDTO filtro) throws IOException {
        var registros = consultaService.consultar(filtro);
        byte[] conteudo;
        String tipo;
        String extensao;

        switch (formato.toLowerCase()) {
            case "csv" -> {
                conteudo = exportacaoService.csv(registros);
                tipo = "text/csv;charset=UTF-8";
                extensao = "csv";
            }
            case "excel" -> {
                conteudo = exportacaoService.excel(registros);
                tipo = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extensao = "xlsx";
            }
            case "pdf" -> {
                conteudo = exportacaoService.pdf(registros);
                tipo = "application/pdf";
                extensao = "pdf";
            }
            default -> {
                return ResponseEntity.badRequest().body(new byte[0]);
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chamados." + extensao)
                .contentType(MediaType.parseMediaType(tipo))
                .body(conteudo);
    }
}