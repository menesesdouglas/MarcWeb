package com.marcweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.folha_ponto.*;
import com.marcweb.entity.*;
import com.marcweb.exceptions.SemRegistrosException;
import com.marcweb.repository.FolhaPontoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FolhaPontoService {


    private final FeriadoService feriadoService;
    private final UserService userService;
    private final RegistroPontoService registroPontoService;
    private final FolgaService folgaService;
    private final FolhaPontoRepository repository;
    private final Detalhes detalhes;


    @Transactional
    public String gerarFolhaPonto(Long userId, int mes, int ano) throws JsonProcessingException {

        var userEntity = userService.findById(userId);
        detalhes.setDetalhes("Folha de ponto do periódo " + mes + "-" + ano + " gerada para o usuário " + userEntity.getUsername());

        var folhaPonto = repository.findByUserIdAndMesAndAno(userId, mes, ano);
        ///  RETORNA OS DADOS SALVOS CASO JÁ EXISTA UMA FOLHA DE PONTO
        ///  DO USUÁRIO PARA O MES E O ANO INFORMADO
        if (folhaPonto != null) return folhaPonto.getDados();

        /// --->>> TODAS OS PONTOS BATIDOS NO MES <<< --- ☻☻☻☻☻☻☻☻☻
        var pontosBatidosNoMes = registroPontoService.findByMesAno(userId, mes, ano);

        var inicioDoMes = LocalDate.of(ano, mes, 1);
        var finalMes = inicioDoMes.withDayOfMonth(inicioDoMes.lengthOfMonth());

        /// --->>> TODAS AS FOLGAS DO MES <<< --- ♥♥♥♥♥♥♥♥♥♥♥♥♥♥♥♥♥
        List<Folga> registrosDeFolgaNoMes = folgaService.findByPeriodo(userId, inicioDoMes, finalMes);

        /// CASO NÃO TENHA NENHUM REGISTRO DE FOLGA OU PONTO BATIDO
        if (registrosDeFolgaNoMes.isEmpty() && pontosBatidosNoMes.isEmpty())
            throw new SemRegistrosException();


        ///  LISTA DE REGISTROS DE PONTO SEPARDOS POR DIA
        List<List<RegistroPonto>> registrosPorDia = agruparRegistrosPorDia(pontosBatidosNoMes);


        // A QUANTIDADE DE LISTAS DE REGISTRO É IGUAL A QUANTIDADE DE DIAS TRABALHADOS
        // JÁ QUE CADA LISTA FOI SEPARADA POR DIA
        var diasTrabalhados = registrosPorDia.size();
        ///  OBJETO QE FARÁ PARTE DO JSON ( COLOQUEI AQUI EM CIMA PARA QUE ELE PUDESSSE SE APROVEITAR DO 'getDiasFalta')
        Periodo periodo = new Periodo(mes, ano, diasTrabalhados);
        var diasFalta = getDiasFalta(userId, diasTrabalhados, inicioDoMes, finalMes, periodo);

        var horasTrabalhadasPorDia = calcularHorarTrabalhadas(registrosPorDia);
        var horasTotais = somarHorasTrabalhadas(horasTrabalhadasPorDia);


        /// GERAR OBJETO QUE SERÁ CONVERTIDO EM JSON
        DadosFolhaPonto dadosFolha = new DadosFolhaPonto();
        dadosFolha.setUsuario(new UsuarioDTO(userId, userEntity.getNome(), userEntity.getUsername()));
        periodo.setDias_uteis(getDiasUteis(inicioDoMes));
        periodo.setDias_falta(diasFalta);
        dadosFolha.setPeriodo(periodo);

        /// ------ TODAS AS DATAS EM QUE O USUÁRIO FOLGOU NO MES  ----- ///
        List<LocalDate> datasFolga = getAllDaysFolgas(registrosDeFolgaNoMes);
        var dias = getDias(registrosPorDia, horasTrabalhadasPorDia, datasFolga);
        dadosFolha.setDias(dias);
        ///  ORDENANDO A LISTA DE DIAS EM ORDEM CRESCENTE
        dadosFolha.organizarDias();

        /// CALCULANDO HORAS EXTRAS E DÉBITO
        BigDecimal horasExtras = calcularHorasExtras(horasTrabalhadasPorDia, registrosPorDia.size());
        BigDecimal horasDebito = calcularHorasDebito(horasTrabalhadasPorDia);
        BigDecimal saldoHoras = horasExtras.subtract(horasDebito);

        /// TOTALIZAÇÃO DE DIAS E HORAS
        Totalizacao totalizacao = new Totalizacao(
                horasTotais,
                horasExtras.intValue(),
                horasDebito.intValue(),
                saldoHoras.intValue()
        );
        dadosFolha.setTotalizacao(totalizacao);

        /// OBJETO USADO PARA TRANSFORMAR OBJETO EM JSON
        ObjectMapper mapper = new ObjectMapper();
        /// CONFIGURAÇÕES PARA QUE ELE   SAIBA TRANSFORMAR AS DATAS EM JSON
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        /// GERANDO JSON
        String json = mapper.writeValueAsString(dadosFolha);

        ///  ENTIDADE 'FOLHA_PONTO' QUE SERÁ PERSISTIDA
        FolhaPonto folha = FolhaPonto.builder()
                .user(userEntity)
                .diasTrabalhados(diasTrabalhados)
                .diasFalta(diasFalta)
                .ano(ano)
                .mes(mes)
                .dados(json)
                .horasTrabalhadas(horasTotais)
                .build();
        repository.save(folha);

        return json;
    }

    ///  TRANSFORMANDO REGSITROS DE FOLGA E PONTO EM INFORMAÇÕES SEPERADAS POR DIA
    private static List<Dia> getDias(List<List<RegistroPonto>> registrosPorDia, List<BigDecimal> horasTrabalhadasPorDia, List<LocalDate> datasFolga) {
        List<Dia> dias = new ArrayList<>();
        /// CRIANDO  DUAS REPETIÇÕES PARA CRIAR INFORMAÇÕES SOBRE OS REGISTROS
        ///  DE FOLGA E DE PONTO
        for (int i = 0; i < registrosPorDia.size(); i++) {
            ///  PASSEI ESSAS DUAS LISTA POIS ACREDITO QUE ESTEJAM SINCRONIZADOS

            Dia dia = new Dia(registrosPorDia.get(i), horasTrabalhadasPorDia.get(i));
            dias.add(dia);
        }
        for (LocalDate dataFolga : datasFolga) {
            Dia dia = new Dia(dataFolga);
            dias.add(dia);
        }
        return dias;
    }


    /// AGRUPANDO REGISTROS POR DIA EM UMA LISTA
    private List<List<RegistroPonto>> agruparRegistrosPorDia(List<RegistroPonto> registros) {
        List<List<RegistroPonto>> listaFinal = new ArrayList<>();
        ///  USADO PARA SEPARAR OS PONTOS BATIDOS DE CADA DIA
        List<RegistroPonto> listaPorDia = new ArrayList<>();
        for (RegistroPonto registro : registros) {
            listaPorDia.add(registro);
            if (registro.getTipo().equals(TipoRegistro.SAIDA)) {
                listaFinal.add(listaPorDia);
                listaPorDia = new ArrayList<>();
            }
        }
        return listaFinal;
    }

    /// CALCULANDO AS HORAS TRABALHADAS EM UM MES
    /// RETORNANDO EM LISTA PARA SEPARAR POR DIA
    private List<BigDecimal> calcularHorarTrabalhadas(List<List<RegistroPonto>> registrosSeparadosPorDia) {
        List<BigDecimal> horasTrabalhadasPorDia = new ArrayList<>();
        for (List<RegistroPonto> registros : registrosSeparadosPorDia) {
            int minutosTrabalhados = 0;

            // COLETANDO REGISTROS DE UM UNICO DIA
            // INDICA QUE O USUARIO BATEU OS QUATRO PONTOS EM ORDEM
            if (registros.size() == 4) {
                var timesTampEntrada = registros.get(0).getTimestamp();
                var timesTampInicioDescanso = registros.get(1).getTimestamp();
                var timesTampFimDescanso = registros.get(2).getTimestamp();
                var timesSaida = registros.get(3).getTimestamp();
                // CALCULANDO HORAS TRABALHADAS NO DIA COM BASE NOS REGISTROS
                minutosTrabalhados = (int) ChronoUnit.MINUTES.between(timesTampEntrada, timesTampInicioDescanso) +
                                     (int) ChronoUnit.MINUTES.between(timesTampFimDescanso, timesSaida);

                // CASO O USUÁRIO TENHA BATIDO APENAS OS PONTOS DE ENTRADA E SAÍDA
            } else if (registros.size() == 2) {
                var timesTampEntrada = registros.get(0).getTimestamp();
                var timesSaida = registros.get(1).getTimestamp();
                // CALCULANDO HORAS TRABALHADAS NO DIA COM BASE NOS REGISTROS
                minutosTrabalhados = (int) ChronoUnit.MINUTES.between(timesTampEntrada, timesSaida);
            }

            // CONVERTENDO MINUTOS PARA HORAS (FORMATO DECIMAL)
            BigDecimal horasTrabalhadas = BigDecimal.valueOf(minutosTrabalhados)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            horasTrabalhadasPorDia.add(horasTrabalhadas);
        }

        return horasTrabalhadasPorDia;
    }

    private BigDecimal somarHorasTrabalhadas(List<BigDecimal> listaDeHoras) {
        BigDecimal horasTrabalhadasNoMes = BigDecimal.ZERO;
        for (BigDecimal horas : listaDeHoras) {
            horasTrabalhadasNoMes = horasTrabalhadasNoMes.add(horas);
        }
        return horasTrabalhadasNoMes;
    }


    // CALCULANDO DIAS DE FALTA
    private int getDiasFalta(Long userId, int diasTrabalhados, LocalDate inicioMes, LocalDate finalMes, Periodo periodo) {
        var qtdDiasMes = inicioMes.getMonth().length(inicioMes.isLeapYear());
        int diasFalta = qtdDiasMes - diasTrabalhados;
        int diasFolga = 0;
        var folgasNoMes = folgaService.findByPeriodo(userId, inicioMes, finalMes);
        for (Folga folga : folgasNoMes) {
            diasFolga += (1 + Integer.parseInt(String.valueOf(ChronoUnit.DAYS.between(folga.getDataInicio(), folga.getDataFim()))));
        }
        periodo.setDias_folga(diasFolga);
        return diasFalta - diasFolga;
    }


    ///  DIAS UTEIS E FINAIS DE SEMANA DO MES
    private int getDiasUteis(LocalDate inicioMes) {
        LocalDate finalMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        int diasUteis = 0;
        for (int i = 0; i < inicioMes.lengthOfMonth(); i++) {
            var diaAtual = inicioMes.plusDays(i);
            System.out.println(diaAtual);
            if (!(diaAtual.getDayOfWeek().equals(DayOfWeek.SATURDAY) || diaAtual.getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                diasUteis += 1;
            }
        }
        ///  QUANTIDADE DE FERIADOS OCORRIDOS NO MES
        var qtdFeriadosNoMes = feriadoService.findByDataBetween(inicioMes, finalMes);
        for (Feriado feriado : qtdFeriadosNoMes) {
            DayOfWeek diaDaSemana = feriado.getData().getDayOfWeek();
            /// SE O FERIADO NÃO FOR NEM NO SABADO E NEM NO DOMINGO, DIMINUI UM DIA ÚTIL
            if (!(diaDaSemana.equals(DayOfWeek.SATURDAY) || diaDaSemana.equals(DayOfWeek.SUNDAY))) {
                diasUteis -= 1;
            }
        }

        return diasUteis;
    }


    /// RECUPERANDO TODAS AS DATAS QE REPRESENTAM FOLGA
    private List<LocalDate> getAllDaysFolgas(List<Folga> folgasNoMes) {
        List<LocalDate> datasDeFolga = new ArrayList<>();
        for (Folga folga : folgasNoMes) {
            var inicioFolga = folga.getDataInicio();
            var finalFolga = folga.getDataFim();
            datasDeFolga.add(inicioFolga);
            datasDeFolga.add(finalFolga);
            /// DISTANCIA ENTRE A DATA DE INICIO E A DATA DE FIM EM DIAS
            long diasCorridos = ChronoUnit.DAYS.between(inicioFolga, finalFolga);
            for (int i = 1; i < diasCorridos; i++) {
                datasDeFolga.add(inicioFolga.plusDays(i));
            }
        }
        return datasDeFolga;
    }

    /// CALCULANDO HORAS EXTRAS
    /// CONFORME LEGISLAÇÃO TRABALHISTA: HORAS EXTRAS SÃO AQUELAS QUE ULTRAPASSAM 8H DIÁRIAS OU 44H SEMANAIS
    private BigDecimal calcularHorasExtras(List<BigDecimal> horasTrabalhadasPorDia, int diasTrabalhados) {
        BigDecimal horasExtras = BigDecimal.ZERO;
        BigDecimal horasDiariasExtras = BigDecimal.ZERO;

        // CALCULAR HORAS EXTRAS DIÁRIAS (ACIMA DE 8H POR DIA)
        for (BigDecimal horasDia : horasTrabalhadasPorDia) {
            if (horasDia.compareTo(BigDecimal.valueOf(8.00)) > 0) {
                horasDiariasExtras = horasDiariasExtras.add(horasDia.subtract(BigDecimal.valueOf(8.00)));
            }
        }

        // CALCULAR HORAS EXTRAS SEMANAIS (ACIMA DE 44H SEMANAIS)
        // Considerando 5 dias úteis por semana em média
        int semanasNoMes = (int) Math.ceil(diasTrabalhados / 5.0);
        BigDecimal limiteSemanal = BigDecimal.valueOf(44.00);
        BigDecimal horasSemanaisExtras = BigDecimal.ZERO;

        // Somar horas trabalhadas no mês
        BigDecimal totalHorasMes = somarHorasTrabalhadas(horasTrabalhadasPorDia);

        // Calcular média semanal
        BigDecimal mediaSemanal = totalHorasMes.divide(BigDecimal.valueOf(semanasNoMes), 2, RoundingMode.HALF_UP);

        // Se a média semanal exceder 44h, calcular horas extras semanais
        if (mediaSemanal.compareTo(limiteSemanal) > 0) {
            horasSemanaisExtras = mediaSemanal.subtract(limiteSemanal).multiply(BigDecimal.valueOf((double) semanasNoMes));
        }

        // Retornar o maior valor entre horas extras diárias e semanais
        horasExtras = horasDiariasExtras.max(horasSemanaisExtras);

        return horasExtras;
    }

    /// CALCULANDO HORAS DÉBITO (HORAS NÃO TRABALHADAS)
    /// CONFORME LEGISLAÇÃO TRABALHISTA: HORAS DÉBITO SÃO AQUELAS QUE FALTAM PARA COMPLETAR A JORNADA
    private BigDecimal calcularHorasDebito(List<BigDecimal> horasTrabalhadasPorDia) {
        BigDecimal horasDebito = BigDecimal.ZERO;

        // CALCULAR HORAS DÉBITO DIÁRIAS (ABAIXO DE 8H POR DIA)
        for (BigDecimal horasDia : horasTrabalhadasPorDia) {
            if (horasDia.compareTo(BigDecimal.valueOf(8.00)) < 0) {
                horasDebito = horasDebito.add(BigDecimal.valueOf(8.00).subtract(horasDia));
            }
        }
        return horasDebito;
    }
}
