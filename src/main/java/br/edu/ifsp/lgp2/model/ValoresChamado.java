package br.edu.ifsp.lgp2.model;

import java.util.List;

public final class ValoresChamado {

    public static final String CATEGORIAS = "Suporte técnico|Documentos|Matrícula|Atendimento acadêmico";
    public static final String STATUS = "ABERTO|EM_ANALISE|CONCLUIDO|CANCELADO";
    public static final String PRIORIDADES = "BAIXA|MEDIA|ALTA|URGENTE";

    public static final List<String> LISTA_CATEGORIAS = List.of(
            "Suporte técnico", "Documentos", "Matrícula", "Atendimento acadêmico"
    );
    public static final List<String> LISTA_PRIORIDADES = List.of(
            "BAIXA", "MEDIA", "ALTA", "URGENTE"
    );

    private ValoresChamado() {
    }
}