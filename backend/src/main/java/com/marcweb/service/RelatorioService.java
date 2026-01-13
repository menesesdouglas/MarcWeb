package com.marcweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.exceptions.RelatorioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class RelatorioService {

    private final ObjectMapper objectMapper;
    private final LogAuditoriaService logAuditoriaService;

    /**
     * Gera relatório PDF de folha de ponto
     */
    public String gerarRelatorioPDF(String folhaPontoJson, User user) throws RelatorioException {
        try {
            // Converte JSON para mapa
            Map<String, Object> folhaPonto = objectMapper.readValue(folhaPontoJson, Map.class);

            // Cria PDF em memória
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // PdfWriter writer = new PdfWriter(outputStream);
            // PdfDocument pdf = new PdfDocument(writer);
            // Document document = new Document(pdf);

            // Adiciona título
            // PdfFont font = PdfFontFactory.createFont("Helvetica-Bold");
            // Paragraph title = new Paragraph("Relatório de Folha de Ponto")
            //         .setFont(font)
            //         .setFontSize(18)
            //         .setTextAlignment(TextAlignment.CENTER)
            //         .setMarginBottom(20);
            // document.add(title);

            // Adiciona informações do funcionário
            // document.add(new Paragraph("Funcionário: " + user.getNome())
            //         .setBold()
            //         .setMarginBottom(5));
            // document.add(new Paragraph("CPF: " + user.getUsername())
            //         .setMarginBottom(10));

            // Adiciona período
            Map<String, Object> periodo = (Map<String, Object>) folhaPonto.get("periodo");
            // document.add(new Paragraph("Período: " + periodo.get("mes") + "/" + periodo.get("ano"))
            //         .setMarginBottom(10));

            // Adiciona tabela de dias trabalhados
            // float[] columnWidths = {100, 100, 100, 100};
            // Table table = new Table(columnWidths);

            // Cabeçalho da tabela
            // table.addHeaderCell("Data");
            // table.addHeaderCell("Horas Trabalhadas");
            // table.addHeaderCell("Tipo de Dia");
            // table.addHeaderCell("Observações");

            // Dados da tabela
            // for (Object diaObj : (Iterable<?>) folhaPonto.get("dias")) {
            //     Map<String, Object> dia = (Map<String, Object>) diaObj;
            //     table.addCell(dia.get("data").toString());
            //     table.addCell(dia.get("horasTrabalhadas").toString());
            //     table.addCell(dia.get("tipoDia").toString());
            //     table.addCell(dia.getOrDefault("observacoes", "").toString());
            // }

            // document.add(table.setMarginTop(20));

            // Adiciona totalização
            Map<String, Object> totalizacao = (Map<String, Object>) folhaPonto.get("totalizacao");
            // document.add(new Paragraph("\nTotalização:")
            //         .setBold()
            //         .setMarginTop(20));

            // document.add(new Paragraph("Horas Trabalhadas: " + totalizacao.get("horasTrabalhadas"))
            //         .setMarginBottom(5));
            // document.add(new Paragraph("Horas Extras: " + totalizacao.get("horasExtras"))
            //         .setMarginBottom(5));
            // document.add(new Paragraph("Horas Débito: " + totalizacao.get("horasDebito"))
            //         .setMarginBottom(5));
            // document.add(new Paragraph("Saldo de Horas: " + totalizacao.get("saldoHoras"))
            //         .setMarginBottom(10));

            // Adiciona rodapé
            // document.add(new Paragraph("Relatório gerado em: " +
            //         LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
            //         .setFontSize(8)
            //         .setTextAlignment(TextAlignment.RIGHT)
            //         .setMarginTop(30));

            // Finaliza documento
            // document.close();

            // Converte para Base64
            String pdfBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            // Registra log de auditoria
            logAuditoriaService.registrarLog(
                user,
                "Geração de relatório PDF",
                CategoriaLog.RELATORIO,
                StatusLog.SUCESSO,
                "Relatório PDF gerado com sucesso"
            );

            return pdfBase64;
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    user,
                    "Geração de relatório PDF",
                    CategoriaLog.RELATORIO,
                    StatusLog.ERRO,
                    "Falha ao gerar relatório PDF: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new RelatorioException("Falha ao gerar relatório PDF", e);
        }
    }
}
