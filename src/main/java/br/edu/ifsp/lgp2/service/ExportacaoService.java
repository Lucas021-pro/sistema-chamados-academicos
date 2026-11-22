package br.edu.ifsp.lgp2.service;

import br.edu.ifsp.lgp2.model.Chamado;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Exportação: recebe os MESMOS registros filtrados pela tela e gera três formatos.
 */
@Service
public class ExportacaoService {

    private static final String[] COLUNAS = {
            "ID", "Título", "Categoria", "Status", "Prioridade", "Responsável", "Abertura"
    };

    // Larguras das 7 colunas somando 770pt (ideal para A4 Paisagem com margens de 36pt)
    private static final float[] LARGURAS_COLUNAS = {45f, 235f, 125f, 90f, 75f, 125f, 75f};

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    // Paleta alinhada ao layout web (Slate + Blue)
    private static final Color COR_PRIMARIA = new Color(37, 99, 235);
    private static final Color COR_CABECALHO_TABELA = new Color(30, 41, 59);
    private static final Color COR_ZEBRA = new Color(248, 250, 252);
    private static final Color COR_BORDA = new Color(226, 232, 240);
    private static final Color COR_TEXTO_PRINCIPAL = new Color(15, 23, 42);
    private static final Color COR_TEXTO_SUAVE = new Color(100, 116, 139);

    private String[] linha(Chamado c) {
        return new String[]{
                String.valueOf(c.getId()),
                texto(c.getTitulo()),
                texto(c.getCategoria()),
                texto(c.getStatus()),
                texto(c.getPrioridade()),
                texto(c.getResponsavel()),
                c.getDataAbertura() == null ? "" : c.getDataAbertura().toString()
        };
    }

    private String[] linhaFormatadaPdf(Chamado c) {
        return new String[]{
                c.getId() != null ? "#" + c.getId() : "-",
                texto(c.getTitulo()),
                texto(c.getCategoria()),
                formatarStatus(c.getStatus()),
                formatarPrioridade(c.getPrioridade()),
                texto(c.getResponsavel()),
                c.getDataAbertura() != null ? c.getDataAbertura().format(DATA_BR) : "-"
        };
    }

    private String texto(String s) {
        return s == null ? "" : s;
    }

    private String formatarStatus(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "ABERTO" -> "Aberto";
            case "EM_ANALISE" -> "Em análise";
            case "CONCLUIDO" -> "Concluído";
            case "CANCELADO" -> "Cancelado";
            default -> status;
        };
    }

    private String formatarPrioridade(String prioridade) {
        if (prioridade == null) return "-";
        return switch (prioridade) {
            case "URGENTE" -> "Urgente";
            case "ALTA" -> "Alta";
            case "MEDIA" -> "Média";
            case "BAIXA" -> "Baixa";
            default -> prioridade;
        };
    }

    /**
     * CSV: UTF-8 com BOM para abrir acentos no Excel; ponto e vírgula como separador.
     */
    public byte[] csv(List<Chamado> itens) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        escreverCsv(sb, COLUNAS);
        for (Chamado c : itens) {
            escreverCsv(sb, linha(c));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void escreverCsv(StringBuilder sb, String[] campos) {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(';');
            String valor = campos[i].replace("\"", "\"\"");
            if (!valor.isEmpty() && "=+-@".indexOf(valor.charAt(0)) >= 0) {
                valor = "'" + valor;
            }
            sb.append('"').append(valor).append('"');
        }
        sb.append("\r\n");
    }

    /**
     * XLSX: Apache POI cria uma pasta de trabalho, uma aba e linhas de células.
     */
    public byte[] excel(List<Chamado> itens) throws IOException {
        try (Workbook livro = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Sheet aba = livro.createSheet("Chamados");
            Font fonte = livro.createFont();
            fonte.setBold(true);
            CellStyle cabecalho = livro.createCellStyle();
            cabecalho.setFont(fonte);

            Row primeira = aba.createRow(0);
            for (int col = 0; col < COLUNAS.length; col++) {
                Cell celula = primeira.createCell(col);
                celula.setCellValue(COLUNAS[col]);
                celula.setCellStyle(cabecalho);
            }

            int numero = 1;
            for (Chamado c : itens) {
                Row linha = aba.createRow(numero++);
                String[] valores = linha(c);
                for (int col = 0; col < valores.length; col++) {
                    String valor = valores[col];
                    if (!valor.isEmpty() && "=+-@".indexOf(valor.charAt(0)) >= 0) {
                        valor = "'" + valor;
                    }
                    linha.createCell(col).setCellValue(valor);
                }
            }

            for (int col = 0; col < COLUNAS.length; col++) {
                aba.setColumnWidth(col, Math.min(40, Math.max(14, COLUNAS[col].length() + 8)) * 256);
            }

            livro.write(saida);
            return saida.toByteArray();
        }
    }

    /**
     * PDF: gera um relatório em paisagem (A4) com cabeçalho, tabela estilizada,
     * quebra automática por célula, efeito zebra e numeração de páginas.
     */
    public byte[] pdf(List<Chamado> itens) throws IOException {
        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PDRectangle formatoPaisagem = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            float margemLateral = 36f;
            float margemSuperior = 40f;
            float margemInferior = 44f;
            float larguraTabela = formatoPaisagem.getWidth() - (2 * margemLateral);

            PDPage pagina = new PDPage(formatoPaisagem);
            documento.addPage(pagina);
            PDPageContentStream canvas = new PDPageContentStream(documento, pagina);

            float yAtual = formatoPaisagem.getHeight() - margemSuperior;
            yAtual = desenharCabecalhoDocumento(canvas, margemLateral, yAtual, larguraTabela, itens.size());
            yAtual = desenharLinhaTabela(
                    canvas, margemLateral, yAtual, COLUNAS, LARGURAS_COLUNAS,
                    PDType1Font.HELVETICA_BOLD, 9f, COR_CABECALHO_TABELA, Color.WHITE, true
            );

            if (itens.isEmpty()) {
                desenharLinhaMensagemVazia(canvas, margemLateral, yAtual, larguraTabela);
            } else {
                for (int i = 0; i < itens.size(); i++) {
                    String[] valores = linhaFormatadaPdf(itens.get(i));
                    float alturaPrevista = calcularAlturaLinha(valores, LARGURAS_COLUNAS, PDType1Font.HELVETICA, 8.5f);

                    if (yAtual - alturaPrevista < margemInferior) {
                        canvas.close();
                        pagina = new PDPage(formatoPaisagem);
                        documento.addPage(pagina);
                        canvas = new PDPageContentStream(documento, pagina);

                        yAtual = formatoPaisagem.getHeight() - margemSuperior;
                        yAtual = desenharLinhaTabela(
                                canvas, margemLateral, yAtual, COLUNAS, LARGURAS_COLUNAS,
                                PDType1Font.HELVETICA_BOLD, 9f, COR_CABECALHO_TABELA, Color.WHITE, true
                        );
                    }

                    Color fundoLinha = (i % 2 == 1) ? COR_ZEBRA : Color.WHITE;
                    yAtual = desenharLinhaTabela(
                            canvas, margemLateral, yAtual, valores, LARGURAS_COLUNAS,
                            PDType1Font.HELVETICA, 8.5f, fundoLinha, COR_TEXTO_PRINCIPAL, false
                    );
                }
            }

            canvas.close();
            numerarPaginas(documento, margemLateral, larguraTabela);

            documento.save(saida);
            return saida.toByteArray();
        }
    }

    private float desenharCabecalhoDocumento(PDPageContentStream canvas, float x, float yTopo,
                                             float larguraTotal, int totalRegistros) throws IOException {
        canvas.beginText();
        canvas.setFont(PDType1Font.HELVETICA_BOLD, 16f);
        canvas.setNonStrokingColor(COR_TEXTO_PRINCIPAL);
        canvas.newLineAtOffset(x, yTopo - 14f);
        canvas.showText(sanitizarPdf("Relatório de Chamados Acadêmicos"));
        canvas.endText();

        String subtitulo = "Gerado em " + LocalDateTime.now().format(DATA_HORA_BR)
                + "   •   Total de registros: " + totalRegistros;
        canvas.beginText();
        canvas.setFont(PDType1Font.HELVETICA, 9f);
        canvas.setNonStrokingColor(COR_TEXTO_SUAVE);
        canvas.newLineAtOffset(x, yTopo - 30f);
        canvas.showText(sanitizarPdf(subtitulo));
        canvas.endText();

        float yLinha = yTopo - 40f;
        canvas.setStrokingColor(COR_PRIMARIA);
        canvas.setLineWidth(1.5f);
        canvas.moveTo(x, yLinha);
        canvas.lineTo(x + larguraTotal, yLinha);
        canvas.stroke();

        return yLinha - 14f;
    }

    private float calcularAlturaLinha(String[] valores, float[] larguras, PDType1Font fonte, float tamanhoFonte) throws IOException {
        float padHorizontal = 7f;
        float padVertical = 6f;
        float entrelinha = 11.5f;
        int maxLinhas = 1;

        for (int i = 0; i < valores.length; i++) {
            float larguraUtil = larguras[i] - (2 * padHorizontal);
            List<String> linhas = quebrarTexto(valores[i], fonte, tamanhoFonte, larguraUtil);
            maxLinhas = Math.max(maxLinhas, linhas.size());
        }

        return Math.max(24f, (maxLinhas * entrelinha) + (2 * padVertical));
    }

    private float desenharLinhaTabela(PDPageContentStream canvas, float xInicial, float yTopo,
                                      String[] valores, float[] larguras, PDType1Font fonte,
                                      float tamanhoFonte, Color corFundo, Color corTexto,
                                      boolean cabecalho) throws IOException {
        float padHorizontal = 7f;
        float entrelinha = 11.5f;

        List<List<String>> colunasQuebradas = new ArrayList<>();
        int maxLinhas = 1;
        for (int i = 0; i < valores.length; i++) {
            float larguraUtil = larguras[i] - (2 * padHorizontal);
            List<String> linhas = quebrarTexto(valores[i], fonte, tamanhoFonte, larguraUtil);
            colunasQuebradas.add(linhas);
            maxLinhas = Math.max(maxLinhas, linhas.size());
        }

        float alturaLinha = cabecalho ? 26f : Math.max(24f, (maxLinhas * entrelinha) + 12f);
        float larguraTotal = 0f;
        for (float l : larguras) {
            larguraTotal += l;
        }

        // 1. Fundo da linha
        canvas.setNonStrokingColor(corFundo);
        canvas.addRect(xInicial, yTopo - alturaLinha, larguraTotal, alturaLinha);
        canvas.fill();

        // 2. Bordas da tabela
        canvas.setStrokingColor(cabecalho ? COR_CABECALHO_TABELA : COR_BORDA);
        canvas.setLineWidth(0.6f);
        canvas.addRect(xInicial, yTopo - alturaLinha, larguraTotal, alturaLinha);
        canvas.stroke();

        float xDivisor = xInicial;
        for (int i = 0; i < larguras.length - 1; i++) {
            xDivisor += larguras[i];
            canvas.moveTo(xDivisor, yTopo);
            canvas.lineTo(xDivisor, yTopo - alturaLinha);
            canvas.stroke();
        }

        // 3. Texto centralizado verticalmente dentro de cada célula
        float xCelula = xInicial;
        for (int i = 0; i < valores.length; i++) {
            List<String> linhas = colunasQuebradas.get(i);
            float alturaBlocoTexto = linhas.size() * entrelinha;
            float offsetSuperior = (alturaLinha - alturaBlocoTexto) / 2f;
            float yTexto = yTopo - offsetSuperior - tamanhoFonte + 2f;

            canvas.setNonStrokingColor(corTexto);
            for (String trecho : linhas) {
                canvas.beginText();
                canvas.setFont(fonte, tamanhoFonte);
                canvas.newLineAtOffset(xCelula + padHorizontal, yTexto);
                canvas.showText(sanitizarPdf(trecho));
                canvas.endText();
                yTexto -= entrelinha;
            }
            xCelula += larguras[i];
        }

        return yTopo - alturaLinha;
    }

    private void desenharLinhaMensagemVazia(PDPageContentStream canvas, float x, float yTopo, float largura) throws IOException {
        float altura = 32f;
        canvas.setNonStrokingColor(COR_ZEBRA);
        canvas.addRect(x, yTopo - altura, largura, altura);
        canvas.fill();

        canvas.setStrokingColor(COR_BORDA);
        canvas.setLineWidth(0.6f);
        canvas.addRect(x, yTopo - altura, largura, altura);
        canvas.stroke();

        canvas.beginText();
        canvas.setFont(PDType1Font.HELVETICA, 9f);
        canvas.setNonStrokingColor(COR_TEXTO_SUAVE);
        canvas.newLineAtOffset(x + 10f, yTopo - 20f);
        canvas.showText(sanitizarPdf("Nenhum chamado encontrado para os filtros selecionados."));
        canvas.endText();
    }

    private void numerarPaginas(PDDocument documento, float margemLateral, float larguraUtil) throws IOException {
        int totalPaginas = documento.getNumberOfPages();
        for (int i = 0; i < totalPaginas; i++) {
            PDPage pagina = documento.getPage(i);
            try (PDPageContentStream rodape = new PDPageContentStream(
                    documento, pagina, PDPageContentStream.AppendMode.APPEND, true, true)) {
                float yLinha = 30f;
                rodape.setStrokingColor(COR_BORDA);
                rodape.setLineWidth(0.5f);
                rodape.moveTo(margemLateral, yLinha);
                rodape.lineTo(margemLateral + larguraUtil, yLinha);
                rodape.stroke();

                rodape.beginText();
                rodape.setFont(PDType1Font.HELVETICA, 8f);
                rodape.setNonStrokingColor(COR_TEXTO_SUAVE);
                rodape.newLineAtOffset(margemLateral, 18f);
                rodape.showText(sanitizarPdf("Sistema de Gestão de Chamados Acadêmicos"));
                rodape.endText();

                String textoPagina = "Página " + (i + 1) + " de " + totalPaginas;
                float larguraTexto = (PDType1Font.HELVETICA.getStringWidth(sanitizarPdf(textoPagina)) / 1000f) * 8f;
                rodape.beginText();
                rodape.setFont(PDType1Font.HELVETICA, 8f);
                rodape.setNonStrokingColor(COR_TEXTO_SUAVE);
                rodape.newLineAtOffset(margemLateral + larguraUtil - larguraTexto, 18f);
                rodape.showText(sanitizarPdf(textoPagina));
                rodape.endText();
            }
        }
    }

    private List<String> quebrarTexto(String textoOriginal, PDType1Font fonte, float tamanhoFonte, float larguraMaxima) throws IOException {
        List<String> linhas = new ArrayList<>();
        String limpo = sanitizarPdf(textoOriginal).trim().replaceAll("\\s+", " ");
        if (limpo.isEmpty()) {
            linhas.add("-");
            return linhas;
        }

        String[] palavras = limpo.split(" ");
        StringBuilder linhaAtual = new StringBuilder();

        for (String palavra : palavras) {
            String candidata = linhaAtual.length() == 0 ? palavra : linhaAtual + " " + palavra;
            float larguraCandidata = (fonte.getStringWidth(candidata) / 1000f) * tamanhoFonte;

            if (larguraCandidata <= larguraMaxima) {
                linhaAtual = new StringBuilder(candidata);
            } else {
                if (linhaAtual.length() > 0) {
                    linhas.add(linhaAtual.toString());
                    linhaAtual = new StringBuilder(palavra);
                } else {
                    StringBuilder pedaco = new StringBuilder();
                    for (char ch : palavra.toCharArray()) {
                        String tentativa = pedaco.toString() + ch;
                        float larguraTentativa = (fonte.getStringWidth(tentativa) / 1000f) * tamanhoFonte;
                        if (larguraTentativa > larguraMaxima && pedaco.length() > 0) {
                            linhas.add(pedaco.toString());
                            pedaco = new StringBuilder(String.valueOf(ch));
                        } else {
                            pedaco.append(ch);
                        }
                    }
                    linhaAtual = pedaco;
                }
            }
        }

        if (linhaAtual.length() > 0) {
            linhas.add(linhaAtual.toString());
        }

        return linhas;
    }

    private String sanitizarPdf(String texto) {
        if (texto == null) {
            return "";
        }
        CharsetEncoder encoder = Charset.forName("windows-1252").newEncoder();
        StringBuilder sb = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            if (ch >= 32 && encoder.canEncode(ch)) {
                sb.append(ch);
            } else if (ch == '\n' || ch == '\r' || ch == '\t') {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}