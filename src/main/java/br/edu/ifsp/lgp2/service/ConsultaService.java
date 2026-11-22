package br.edu.ifsp.lgp2.service;

import br.edu.ifsp.lgp2.dto.FiltroChamadoDTO;
import br.edu.ifsp.lgp2.dto.IndicadoresDTO;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Camada Service: concentra consultas dinâmicas e regras de indicadores.
 */
@Service
public class ConsultaService {

    private final ChamadoRepository repository;

    public ConsultaService(ChamadoRepository repository) {
        this.repository = repository;
    }

    public List<Chamado> consultar(FiltroChamadoDTO filtro) {
        return repository.findAll(montarEspecificacao(filtro), Sort.by(Sort.Direction.DESC, "id"));
    }

    public Page<Chamado> consultarPagina(FiltroChamadoDTO filtro, int pagina, int tamanho) {
        int paginaSegura = Math.max(pagina, 0);
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), 100);
        Pageable pageable = PageRequest.of(paginaSegura, tamanhoSeguro, Sort.by(Sort.Direction.DESC, "id"));
        return repository.findAll(montarEspecificacao(filtro), pageable);
    }

    private Specification<Chamado> montarEspecificacao(FiltroChamadoDTO filtro) {
        Specification<Chamado> regra = (raiz, query, cb) -> cb.conjunction();

        if (filtro != null) {
            if (filtro.getBusca() != null && !filtro.getBusca().isBlank()) {
                regra = regra.and(criarRegraBusca(filtro.getBusca()));
            }
            if (filtro.getStatus() != null && !filtro.getStatus().isBlank()) {
                regra = regra.and((r, q, cb) -> cb.equal(r.get("status"), filtro.getStatus()));
            }
            if (filtro.getCategoria() != null && !filtro.getCategoria().isBlank()) {
                regra = regra.and((r, q, cb) -> cb.equal(r.get("categoria"), filtro.getCategoria()));
            }
            if (filtro.getPrioridade() != null && !filtro.getPrioridade().isBlank()) {
                regra = regra.and((r, q, cb) -> cb.equal(r.get("prioridade"), filtro.getPrioridade()));
            }
            if (filtro.getInicio() != null) {
                regra = regra.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("dataAbertura"), filtro.getInicio()));
            }
            if (filtro.getFim() != null) {
                regra = regra.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("dataAbertura"), filtro.getFim()));
            }
        }

        return regra;
    }

    private Specification<Chamado> criarRegraBusca(String buscaBruta) {
        String bruto = buscaBruta.trim();
        boolean apenasId = bruto.startsWith("#");
        String semHash = apenasId ? bruto.substring(1).trim() : bruto;

        Long idBuscado = null;
        if (!semHash.isEmpty() && semHash.chars().allMatch(Character::isDigit)) {
            try {
                idBuscado = Long.parseLong(semHash);
            } catch (NumberFormatException ignored) {
            }
        }

        if (apenasId) {
            Long idFinal = idBuscado != null ? idBuscado : -1L;
            return (r, q, cb) -> cb.equal(r.get("id"), idFinal);
        }

        String termo = "%" + bruto.toLowerCase(Locale.ROOT) + "%";
        if (idBuscado != null) {
            Long idFinal = idBuscado;
            return (r, q, cb) -> cb.or(
                    cb.equal(r.get("id"), idFinal),
                    cb.like(cb.lower(r.get("titulo")), termo),
                    cb.like(cb.lower(r.get("descricao")), termo)
            );
        }

        return (r, q, cb) -> cb.or(
                cb.like(cb.lower(r.get("titulo")), termo),
                cb.like(cb.lower(r.get("descricao")), termo)
        );
    }

    public IndicadoresDTO indicadores() {
        return calcular(repository.findAll());
    }

    public IndicadoresDTO indicadores(FiltroChamadoDTO filtro) {
        return calcular(consultar(filtro));
    }

    private IndicadoresDTO calcular(List<Chamado> itens) {
        long abertos = contar(itens, "ABERTO");
        long analise = contar(itens, "EM_ANALISE");
        long concluidos = contar(itens, "CONCLUIDO");
        long cancelados = contar(itens, "CANCELADO");
        long altas = itens.stream()
                .filter(c -> "ALTA".equals(c.getPrioridade()) || "URGENTE".equals(c.getPrioridade()))
                .count();
        double percentual = itens.isEmpty() ? 0 : concluidos * 100.0 / itens.size();
        return new IndicadoresDTO(itens.size(), abertos, analise, concluidos, cancelados, altas, percentual);
    }

    private long contar(List<Chamado> itens, String status) {
        return itens.stream().filter(c -> status.equals(c.getStatus())).count();
    }
}