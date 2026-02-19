package com.marcweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.marcweb.entity.Feriado;
import com.marcweb.entity.Folga;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.entity.TipoFolga;
import com.marcweb.exceptions.FolhaPontoNotFoundException;
import com.marcweb.exceptions.RelatorioException;
import com.marcweb.exceptions.SemRegistrosException;
import com.marcweb.dto.folha_ponto.DadosFolhaPonto;
import com.marcweb.dto.folha_ponto.Dia;
import com.marcweb.dto.folha_ponto.Periodo;
import com.marcweb.dto.folha_ponto.RegistroPontoDTO;
import com.marcweb.dto.folha_ponto.Totalizacao;
import com.marcweb.dto.folha_ponto.UsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class RelatorioService {

    private final ObjectMapper objectMapper;
    private final LogAuditoriaService logAuditoriaService;
    private final FolhaPontoService folhaPontoService;
    private final FeriadoService feriadoService;
    private final FolgaService folgaService;
    private final UserService userService;

    /**
     * Gera relatório PDF de folha de ponto com layout completo
     */
    public String gerarRelatorioPDF(Long userId, int mes, int ano) throws RelatorioException {
        User user = userService.findById(userId);

        // Gera a folha de ponto internamente
        String folhaPontoJson = folhaPontoService.buscarFolhaPonto(userId, mes, ano);

        try {

            // Converte JSON para objeto
            DadosFolhaPonto folhaPonto = objectMapper.readValue(folhaPontoJson, DadosFolhaPonto.class);

            // Cria PDF em memória
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Configurações de fonte
            PdfFont font = PdfFontFactory.createFont();
            PdfFont boldFont = PdfFontFactory.createFont();

            // Cabeçalho moderno do PDF
            adicionarCabecalhoModerno(document, user, mes, ano, font, boldFont);

            // Seção de resumo executivo
            adicionarResumoExecutivo(document, folhaPonto, font, boldFont);

            // Tabela principal com todos os dias do mês
            Table table = criarTabelaPrincipal(folhaPonto, font, boldFont);
            document.add(table.setMarginTop(10));

            // Totalização
            adicionarTotalizacao(document, folhaPonto, font, boldFont);

            // Espaço para assinatura
            adicionarEspacoAssinatura(document, user, font, boldFont);

            // Rodapé
            adicionarRodape(document, font);

            // Finaliza documento
            document.close();

            // Converte para Base64
            String pdfBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            // Registra log de auditoria
            logAuditoriaService.registrarLog(
                    user,
                    "Geração de relatório PDF",
                    CategoriaLog.USO_SISTEMA,
                    StatusLog.SUCESSO,
                    "Relatório PDF gerado com sucesso - Período: " + mes + "/" + ano
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

    /**
     * Cabeçalho moderno do PDF com design profissional
     */
    private void adicionarCabecalhoModerno(Document document, User user, int mes, int ano, PdfFont font, PdfFont boldFont) {
        // Linha divisória superior
        document.add(new Paragraph("").setMarginBottom(5));

        // Título principal com design moderno
        Paragraph title = new Paragraph("MarcWeb - TechHub Soluções Tecnológicas")
                .setFont(boldFont)
                .setFontSize(22)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE)
                .setMarginBottom(3);
        document.add(title);

        // Subtítulo
        Paragraph subtitle = new Paragraph("Folha de Ponto Mensal")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(8);
        document.add(subtitle);

        // Linha divisória decorativa
        document.add(new Paragraph("").setMarginBottom(5));

        // Informações do funcionário em layout de duas colunas
        Table infoTable = new Table(2);
        infoTable.setWidth(520f);

        // Coluna esquerda - Dados do funcionário
        Cell cell1 = new Cell().add(new Paragraph("Funcionário:").setFont(boldFont))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingRight(5);
        infoTable.addCell(cell1);

        Cell cell2 = new Cell().add(new Paragraph(user.getNome()).setFont(font))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        infoTable.addCell(cell2);

        Cell cell3 = new Cell().add(new Paragraph("CPF:").setFont(boldFont))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingRight(5);
        infoTable.addCell(cell3);

        Cell cell4 = new Cell().add(new Paragraph(user.getCpf()).setFont(font))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        infoTable.addCell(cell4);

        // Coluna direita - Período e cargo
        Cell cell5 = new Cell().add(new Paragraph("Período:").setFont(boldFont))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingRight(5);
        infoTable.addCell(cell5);

        Cell cell6 = new Cell().add(new Paragraph(String.format("%02d", mes) + "/" + ano).setFont(font))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        infoTable.addCell(cell6);

        Cell cell7 = new Cell().add(new Paragraph("Cargo:").setFont(boldFont))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingRight(5);
        infoTable.addCell(cell7);

        Cell cell8 = new Cell().add(new Paragraph(user.getCargo() != null ? user.getCargo() : "Não informado").setFont(font))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        infoTable.addCell(cell8);

        document.add(infoTable.setMarginBottom(10));
    }

    /**
     * Seção de resumo executivo com métricas rápidas
     */
    private void adicionarResumoExecutivo(Document document, DadosFolhaPonto folhaPonto, PdfFont font, PdfFont boldFont) {
        // Título da seção
        Paragraph sectionTitle = new Paragraph("Resumo Executivo")
                .setFont(boldFont)
                .setFontSize(16)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela de resumo
        Table resumoTable = new Table(4);
        resumoTable.setWidth(520f);

        // Cabeçalho da tabela de resumo
        Cell headerCell = new Cell(1, 4).add(new Paragraph("Métricas do Período").setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        resumoTable.addHeaderCell(headerCell);

        // Linha 1: Horas trabalhadas e horas extras
        Cell cell1 = new Cell().add(new Paragraph("Horas Trabalhadas").setFont(boldFont))
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE);
        resumoTable.addCell(cell1);

        Cell cell2 = new Cell().add(new Paragraph(folhaPonto.getTotalizacao().getHoras_trabalhadas().toString()).setFont(font));
        resumoTable.addCell(cell2);

        Cell cell3 = new Cell().add(new Paragraph("Horas Extras").setFont(boldFont))
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE);
        resumoTable.addCell(cell3);

        Cell cell4 = new Cell().add(new Paragraph(folhaPonto.getTotalizacao().getHoras_extras() + " h").setFont(font));
        resumoTable.addCell(cell4);

        // Linha 2: Horas débito e saldo
        Cell cell5 = new Cell().add(new Paragraph("Horas Débito").setFont(boldFont))
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE);
        resumoTable.addCell(cell5);

        Cell cell6 = new Cell().add(new Paragraph(folhaPonto.getTotalizacao().getHoras_debito() + " h").setFont(font));
        resumoTable.addCell(cell6);

        Cell cell7 = new Cell().add(new Paragraph("Saldo de Horas").setFont(boldFont))
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE);
        resumoTable.addCell(cell7);

        Cell cell8 = new Cell().add(new Paragraph(folhaPonto.getTotalizacao().getSaldo_horas() + " h").setFont(font));
        resumoTable.addCell(cell8);

        document.add(resumoTable.setMarginBottom(10));
    }

    /**
     * Espaço para assinatura no final do documento
     */
    private void adicionarEspacoAssinatura(Document document, User user, PdfFont font, PdfFont boldFont) {
        // Linha divisória
        document.add(new Paragraph("").setMarginTop(30).setMarginBottom(10));

        // Título da seção de assinatura
        Paragraph signatureTitle = new Paragraph("Assinatura do Funcionário")
                .setFont(boldFont)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(signatureTitle);

        // Linha para assinatura
        Table signatureTable = new Table(1);
        signatureTable.setWidth(520f);

        Cell signatureCell = new Cell().add(new Paragraph("_________________________________________").setFont(boldFont))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        signatureTable.addCell(signatureCell);

        Cell nameCell = new Cell().add(new Paragraph("Nome: " + user.getNome()).setFont(font))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5);
        signatureTable.addCell(nameCell);

        Cell dateCell = new Cell().add(new Paragraph("Data: ____/____/________").setFont(font))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5);
        signatureTable.addCell(dateCell);

        document.add(signatureTable.setMarginTop(10));
    }

    /**
     * Cabeçalho antigo (mantido para compatibilidade)
     */
    private void adicionarCabecalho(Document document, User user, int mes, int ano, PdfFont font) {
        // Título principal
        Paragraph title = new Paragraph("MarcWeb - TechHub Soluções Tecnológicas")
                .setFont(font)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE)
                .setMarginBottom(5);
        document.add(title);

        // Subtítulo
        Paragraph subtitle = new Paragraph("Folha de Ponto Mensal")
                .setFont(font)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);

        // Informações do funcionário
        document.add(new Paragraph("Funcionário: " + user.getNome())
                .setFont(font)
                .setBold()
                .setMarginBottom(3));
        document.add(new Paragraph("CPF: " + user.getCpf())
                .setFont(font)
                .setMarginBottom(3));
        document.add(new Paragraph("Período: " + String.format("%02d", mes) + "/" + ano)
                .setFont(font)
                .setMarginBottom(15));
    }

    private Table criarTabelaPrincipal(DadosFolhaPonto folhaPonto, PdfFont font, PdfFont boldFont) {
        // Configuração das colunas
        float[] columnWidths = {60, 80, 80, 80, 80, 100, 120};
        Table table = new Table(columnWidths);

        // Cabeçalho da tabela
        adicionarCabecalhoTabela(table, boldFont);

        // Obter todos os dias do mês
        int diasNoMes = LocalDate.of(folhaPonto.getPeriodo().getAno(), folhaPonto.getPeriodo().getMes(), 1).lengthOfMonth();
        LocalDate inicioMes = LocalDate.of(folhaPonto.getPeriodo().getAno(), folhaPonto.getPeriodo().getMes(), 1);
        LocalDate finalMes = inicioMes.withDayOfMonth(diasNoMes);

        // Obter feriados e folgas do período
        List<Feriado> feriados = feriadoService.findByDataBetween(inicioMes, finalMes);
        List<Folga> folgas = folgaService.findByPeriodo(folhaPonto.getUsuario().getId(), inicioMes, finalMes);

        // Preencher todos os dias do mês
        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate dataAtual = LocalDate.of(folhaPonto.getPeriodo().getAno(), folhaPonto.getPeriodo().getMes(), dia);
            adicionarLinhaDia(table, dataAtual, folhaPonto.getDias(), feriados, folgas, font, boldFont);
        }

        return table;
    }

    private void adicionarCabecalhoTabela(Table table, PdfFont boldFont) {
        // Cabeçalho da tabela com design moderno
        Cell cell = new Cell().add(new Paragraph("Data").setFont(boldFont)).setBold()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addHeaderCell(cell);

        cell = new Cell().add(new Paragraph("Entrada").setFont(boldFont)).setBold()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addHeaderCell(cell);

        cell = new Cell().add(new Paragraph("Início\nDescanso").setFont(boldFont)).setBold()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addHeaderCell(cell);

        cell = new Cell().add(new Paragraph("Fim\nDescanso").setFont(boldFont)).setBold()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addHeaderCell(cell);

        cell = new Cell().add(new Paragraph("Saída").setFont(boldFont)).setBold()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addHeaderCell(cell);

        cell = new Cell().add(new Paragraph("Tipo de Dia").setFont(boldFont)).setBold()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addHeaderCell(cell);

        cell = new Cell().add(new Paragraph("Observações").setFont(boldFont)).setBold()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addHeaderCell(cell);
    }

    private void adicionarLinhaDia(Table table, LocalDate data, List<Dia> diasRegistrados, List<Feriado> feriados, List<Folga> folgas, PdfFont font, PdfFont boldFont) {
        // Verifica o tipo de dia
        TipoDia tipoDia = identificarTipoDia(data, feriados, folgas);
        String observacoes = getObservacoes(data, tipoDia, feriados, folgas);

        // Busca registros do dia
        Dia diaRegistrado = buscarDiaRegistrado(data, diasRegistrados);
        List<RegistroPontoDTO> registros = (diaRegistrado != null) ? diaRegistrado.getRegistros() : new ArrayList<>();

        // Cria células com formatação moderna
        Cell cell;

        // Coluna Data com formatação moderna
        String diaSemana = data.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")).toUpperCase();
        cell = new Cell().add(new Paragraph(data.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + diaSemana).setFont(font))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3);
        cell.setBackgroundColor(tipoDia.getCor());
        table.addCell(cell);

        // Colunas de horários com formatação consistente
        String entrada = getHorarioPorTipo(registros, "ENTRADA");
        String inicioDescanso = getHorarioPorTipo(registros, "INICIO_DESCANSO");
        String fimDescanso = getHorarioPorTipo(registros, "FIM_DESCANSO");
        String saida = getHorarioPorTipo(registros, "SAIDA");

        cell = new Cell().add(new Paragraph(entrada).setFont(font))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3);
        cell.setBackgroundColor(tipoDia.getCor());
        table.addCell(cell);

        cell = new Cell().add(new Paragraph(inicioDescanso).setFont(font))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3);
        cell.setBackgroundColor(tipoDia.getCor());
        table.addCell(cell);

        cell = new Cell().add(new Paragraph(fimDescanso).setFont(font))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3);
        cell.setBackgroundColor(tipoDia.getCor());
        table.addCell(cell);

        cell = new Cell().add(new Paragraph(saida).setFont(font))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3);
        cell.setBackgroundColor(tipoDia.getCor());
        table.addCell(cell);

        // Coluna Tipo de Dia com destaque
        cell = new Cell().add(new Paragraph(tipoDia.getDescricao()).setFont(boldFont)).setBold()
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3);
        cell.setBackgroundColor(tipoDia.getCor());
        table.addCell(cell);

        // Coluna Observações com formatação limpa
        cell = new Cell().add(new Paragraph(observacoes).setFont(font))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3);
        cell.setBackgroundColor(tipoDia.getCor());
        table.addCell(cell);
    }

    private TipoDia identificarTipoDia(LocalDate data, List<Feriado> feriados, List<Folga> folgas) {
        // Verifica se é final de semana
        if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return TipoDia.FINAL_DE_SEMANA;
        }

        // Verifica se é feriado
        for (Feriado feriado : feriados) {
            if (feriado.getData().equals(data)) {
                return TipoDia.FERIADO;
            }
        }

        // Verifica se é folga
        for (Folga folga : folgas) {
            if (!data.isBefore(folga.getDataInicio()) && !data.isAfter(folga.getDataFim())) {
                return TipoDia.FOLGA;
            }
        }

        // Verifica se é falta (não tem registro e não é final de semana, feriado ou folga)
        // Isso será tratado na lógica de observações

        return TipoDia.DIA_UTIL;
    }

    private String getObservacoes(LocalDate data, TipoDia tipoDia, List<Feriado> feriados, List<Folga> folgas) {
        StringBuilder obs = new StringBuilder();

        switch (tipoDia) {
            case FERIADO:
                for (Feriado feriado : feriados) {
                    if (feriado.getData().equals(data)) {
                        obs.append("Feriado: ").append(feriado.getNome());
                        if (feriado.getOrigem() != null) {
                            obs.append(" (").append(feriado.getOrigem().name()).append(")");
                        }
                        break;
                    }
                }
                break;
            case FOLGA:
                for (Folga folga : folgas) {
                    if (!data.isBefore(folga.getDataInicio()) && !data.isAfter(folga.getDataFim())) {
                        obs.append("Folga: ").append(folga.getTipo().name());
                        if (folga.getJustificativa() != null && !folga.getJustificativa().isEmpty()) {
                            obs.append(" - ").append(folga.getJustificativa());
                        }
                        break;
                    }
                }
                break;
            case FALTA:
                obs.append("Falta não justificada");
                break;
            case DIA_UTIL:
                obs.append("Dia útil normal");
                break;
            case FINAL_DE_SEMANA:
                obs.append("Final de semana");
                break;
        }

        return obs.toString();
    }

    private Dia buscarDiaRegistrado(LocalDate data, List<Dia> dias) {
        for (Dia dia : dias) {
            if (dia.getData().equals(data)) {
                return dia;
            }
        }
        return null;
    }

    private String getHorarioPorTipo(List<RegistroPontoDTO> registros, String tipo) {
        for (RegistroPontoDTO registro : registros) {
            if (registro.getTipo().name().equals(tipo)) {
                return registro.getTimestamp().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        }
        return "-";
    }

    private void adicionarTotalizacao(Document document, DadosFolhaPonto folhaPonto, PdfFont font, PdfFont boldFont) {
        document.add(new Paragraph("\nTotalização do Período:")
                .setFont(boldFont)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(5));

        document.add(new Paragraph("Horas Trabalhadas: " + folhaPonto.getTotalizacao().getHoras_trabalhadas())
                .setFont(font)
                .setMarginBottom(3));
        document.add(new Paragraph("Horas Extras: " + folhaPonto.getTotalizacao().getHoras_extras())
                .setFont(font)
                .setMarginBottom(3));
        document.add(new Paragraph("Horas Débito: " + folhaPonto.getTotalizacao().getHoras_debito())
                .setFont(font)
                .setMarginBottom(5));
    }

    private void adicionarRodape(Document document, PdfFont font) {
        document.add(new Paragraph("Relatório gerado em: " +
                                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(15));
    }

    private String criarFolhaPontoVazia(Long userId, int mes, int ano) throws Exception {
        User user = userService.findById(userId);
        DadosFolhaPonto folha = new DadosFolhaPonto();
        folha.setUsuario(new UsuarioDTO(userId, user.getNome(), user.getUsername(), user.getCpf()));
        folha.setPeriodo(new Periodo(mes, ano, 0));
        folha.setDias(new ArrayList<>());
        folha.setTotalizacao(new Totalizacao(BigDecimal.ZERO, 0, 0, 0));
        return objectMapper.writeValueAsString(folha);
    }

    // Enum para tipos de dia com cores modernas
    private enum TipoDia {
        DIA_UTIL("DIA ÚTIL", ColorConstants.WHITE),
        FALTA("FALTA", new com.itextpdf.kernel.colors.DeviceRgb(255, 182, 193)), // Rosa claro
        FOLGA("FOLGA", new com.itextpdf.kernel.colors.DeviceRgb(144, 238, 144)), // Verde claro
        FERIADO("FERIADO", new com.itextpdf.kernel.colors.DeviceRgb(255, 215, 0)), // Amarelo dourado
        FINAL_DE_SEMANA("FINAL DE SEMANA", new com.itextpdf.kernel.colors.DeviceRgb(211, 211, 211)); // Cinza claro

        private final String descricao;
        private final com.itextpdf.kernel.colors.Color cor;

        TipoDia(String descricao, com.itextpdf.kernel.colors.Color cor) {
            this.descricao = descricao;
            this.cor = cor;
        }

        public String getDescricao() {
            return descricao;
        }

        public com.itextpdf.kernel.colors.Color getCor() {
            return cor;
        }
    }
}
