package br.edu.ifsp.lgp2.service;

import br.edu.ifsp.lgp2.dto.ChamadoAtualizacaoStatusDTO;
import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.dto.ChamadoEdicaoDTO;
import br.edu.ifsp.lgp2.factory.ChamadoFactory;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/*
- Service centraliza as operações e regras de escrita do sistema.
 */
@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    public Chamado buscarPorId(Long id) {
        return chamadoRepository.findById(id).orElse(null);
    }

    /** Optional explicita ao Controller REST a possibilidade de não encontrar o recurso. */
    public Optional<Chamado> buscarOpcional(Long id) {
        return chamadoRepository.findById(id);
    }

    public boolean existe(Long id) {
        return chamadoRepository.existsById(id);
    }

    public Chamado salvarNovoChamado(ChamadoCadastroDTO dto) {
        Chamado chamado = ChamadoFactory.criar(dto);
        return chamadoRepository.save(chamado);
    }

    /*
- Edição geral do chamado (web e PUT /api/chamados/{id}).
- Altera somente os dados do ChamadoEdicaoDTO; status, observação de
- atendimento e data de abertura são preservados.
     */
    public Optional<Chamado> atualizarDados(Long id, ChamadoEdicaoDTO dto) {
        return chamadoRepository.findById(id).map(chamado -> {
            chamado.setTitulo(dto.getTitulo());
            chamado.setDescricao(dto.getDescricao());
            chamado.setCategoria(dto.getCategoria());
            chamado.setPrioridade(dto.getPrioridade());
            chamado.setResponsavel(dto.getResponsavel());
            chamado.setDataAtualizacao(LocalDate.now());

            chamado.registrarHistorico(
                    chamado.getStatus(),
                    chamado.getResponsavel(),
                    "Dados da solicitação atualizados."
            );

            return chamadoRepository.save(chamado);
        });
    }

    /*
- Atualiza apenas status e observação de atendimento.
- O DTO protege os demais campos da Entity.
     */
    public Chamado atualizarStatus(ChamadoAtualizacaoStatusDTO dto) {
        Chamado chamado = chamadoRepository.findById(dto.getId()).orElse(null);

        if (chamado == null) {
            return null;
        }

        chamado.setStatus(dto.getStatus());
        chamado.setObservacaoAtendimento(dto.getObservacaoAtendimento());
        chamado.setDataAtualizacao(LocalDate.now());

        chamado.registrarHistorico(
                dto.getStatus(),
                chamado.getResponsavel(),
                dto.getObservacaoAtendimento()
        );

        return chamadoRepository.save(chamado);
    }

    public void excluir(Long id) {
        chamadoRepository.deleteById(id);
    }
}