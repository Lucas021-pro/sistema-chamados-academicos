package br.edu.ifsp.lgp2.service;

import br.edu.ifsp.lgp2.dto.ChamadoCadastroDTO;
import br.edu.ifsp.lgp2.factory.ChamadoFactory;
import br.edu.ifsp.lgp2.model.Chamado;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportacaoServiceTest {

    private final ExportacaoService exportacaoService = new ExportacaoService();

    private List<Chamado> amostra() {
        Chamado c = ChamadoFactory.criar(new ChamadoCadastroDTO(
                "=SOMA(1;2) Teste fórmula",
                "Descrição para exportação.",
                "Suporte técnico"
        ));
        c.setId(1L);
        return List.of(c);
    }

    @Test
    void deveGerarCsvComBomEProtecaoContraFormulaInjection() {
        byte[] bytes = exportacaoService.csv(amostra());
        String conteudo = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(conteudo.startsWith("\uFEFF\"ID\";\"Título\";\"Categoria\";\"Status\";\"Prioridade\";\"Responsável\";\"Abertura\""));
        assertTrue(conteudo.contains("\"'=SOMA(1;2) Teste fórmula\""));
    }

    @Test
    void deveGerarArquivoExcelValido() throws Exception {
        byte[] bytes = exportacaoService.excel(amostra());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals("Chamados", workbook.getSheetName(0));
            assertEquals("ID", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            assertEquals("1", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        }
    }

    @Test
    void deveGerarPdfValido() throws Exception {
        byte[] bytes = exportacaoService.pdf(amostra());
        String cabecalhoPdf = new String(bytes, 0, 4, StandardCharsets.US_ASCII);

        assertEquals("%PDF", cabecalhoPdf);
    }
}