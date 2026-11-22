package br.edu.ifsp.lgp2.controller;

import br.edu.ifsp.lgp2.dto.ChamadoAtualizacaoStatusDTO;
import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.dto.ChamadoEdicaoDTO;
import br.edu.ifsp.lgp2.dto.FiltroChamadoDTO;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.service.ChamadoService;
import br.edu.ifsp.lgp2.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/*
 * Controller cuida das rotas, telas, formulários e mensagens.
 */
@Controller
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final ConsultaService consultaService;

    public ChamadoController(ChamadoService chamadoService, ConsultaService consultaService) {
        this.chamadoService = chamadoService;
        this.consultaService = consultaService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/chamados";
    }

    @GetMapping("/chamados")
    public String listar(@ModelAttribute("filtro") FiltroChamadoDTO filtro,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {
        Page<Chamado> paginaChamados = consultaService.consultarPagina(filtro, page, size);

        // Se o usuário pediu uma página além do total existente (ex.: após excluir o último item da página)
        if (page > 0 && paginaChamados.getContent().isEmpty() && paginaChamados.getTotalElements() > 0) {
            int ultimaPagina = Math.max(paginaChamados.getTotalPages() - 1, 0);
            paginaChamados = consultaService.consultarPagina(filtro, ultimaPagina, size);
        }

        List<Chamado> todosFiltrados = consultaService.consultar(filtro);
        long aguardando = todosFiltrados.stream()
                .filter(c -> "ABERTO".equals(c.getStatus()) || "EM_ANALISE".equals(c.getStatus()))
                .count();

        model.addAttribute("chamados", paginaChamados.getContent());
        model.addAttribute("pagina", paginaChamados);
        model.addAttribute("totalFiltrados", todosFiltrados.size());
        model.addAttribute("aguardando", aguardando);
        model.addAttribute("filtro", filtro);
        return "chamados/lista";
    }

    @GetMapping("/chamados/novo")
    public String novo(Model model) {
        model.addAttribute("chamadoCadastroDTO", new ChamadoCadastroDTO());
        return "chamados/formulario-cadastro";
    }

    /*
     * A validação do DTO (Bean Validation) substitui a checagem manual do título
     * e também cobre descrição e categoria.
     */
    @PostMapping("/chamados/salvar")
    public String salvarNovo(@Valid @ModelAttribute("chamadoCadastroDTO") ChamadoCadastroDTO dto,
                             BindingResult resultado,
                             RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return "chamados/formulario-cadastro";
        }

        chamadoService.salvarNovoChamado(dto);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Chamado cadastrado com sucesso.");
        return "redirect:/chamados";
    }

    @GetMapping("/chamados/editar/{id}")
    public String editar(@PathVariable Long id,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        Chamado chamado = chamadoService.buscarPorId(id);

        if (chamado == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Chamado não encontrado.");
            return "redirect:/chamados";
        }

        ChamadoEdicaoDTO dto = new ChamadoEdicaoDTO();
        dto.setTitulo(chamado.getTitulo());
        dto.setDescricao(chamado.getDescricao());
        dto.setCategoria(chamado.getCategoria());
        dto.setPrioridade(chamado.getPrioridade());
        dto.setResponsavel(chamado.getResponsavel());

        model.addAttribute("chamadoId", id);
        model.addAttribute("chamadoEdicaoDTO", dto);
        return "chamados/formulario-edicao";
    }

    @PostMapping("/chamados/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("chamadoEdicaoDTO") ChamadoEdicaoDTO dto,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            model.addAttribute("chamadoId", id);
            return "chamados/formulario-edicao";
        }

        if (chamadoService.atualizarDados(id, dto).isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Chamado não encontrado.");
            return "redirect:/chamados";
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Chamado atualizado com sucesso.");
        return "redirect:/chamados";
    }

    /*
     * Rota da atividade:
     * abre o formulário específico para atualização de status.
     */
    @GetMapping("/chamados/status/{id}")
    public String formularioStatus(@PathVariable Long id,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        Chamado chamado = chamadoService.buscarPorId(id);

        if (chamado == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Chamado não encontrado.");
            return "redirect:/chamados";
        }

        ChamadoAtualizacaoStatusDTO dto = new ChamadoAtualizacaoStatusDTO();
        dto.setId(chamado.getId());
        dto.setStatus(chamado.getStatus());
        dto.setObservacaoAtendimento(chamado.getObservacaoAtendimento());

        model.addAttribute("statusDTO", dto);
        model.addAttribute("chamado", chamado);

        return "chamados/formulario-status";
    }

    /*
     * Rota da atividade:
     * recebe o DTO, valida (status permitido, observação até 500 caracteres)
     * e atualiza apenas status e observação.
     */
    @PostMapping("/chamados/status/atualizar")
    public String atualizarStatus(@Valid @ModelAttribute ChamadoAtualizacaoStatusDTO dto,
                                  BindingResult resultado,
                                  RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro", primeiraMensagem(resultado));
            return dto.getId() == null ? "redirect:/chamados" : "redirect:/chamados/status/" + dto.getId();
        }

        Chamado chamado = chamadoService.atualizarStatus(dto);

        if (chamado == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Chamado não encontrado.");
            return "redirect:/chamados";
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Status atualizado com sucesso.");
        return "redirect:/chamados/detalhes/" + chamado.getId();
    }

    @GetMapping("/chamados/detalhes/{id}")
    public String detalhes(@PathVariable Long id,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        Chamado chamado = chamadoService.buscarPorId(id);

        if (chamado == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Chamado não encontrado.");
            return "redirect:/chamados";
        }

        model.addAttribute("chamado", chamado);
        return "chamados/detalhes";
    }

    @PostMapping("/chamados/excluir/{id}")
    public String excluir(@PathVariable Long id,
                          RedirectAttributes redirectAttributes) {

        if (!chamadoService.existe(id)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Chamado não encontrado para exclusão.");
            return "redirect:/chamados";
        }

        chamadoService.excluir(id);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Chamado excluído com sucesso.");
        return "redirect:/chamados";
    }

    /* Mensagem do primeiro erro de validação, exibida na página com o mesmo estilo de antes. */
    private String primeiraMensagem(BindingResult resultado) {
        return resultado.getAllErrors().get(0).getDefaultMessage();
    }


}
