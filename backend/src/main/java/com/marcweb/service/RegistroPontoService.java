package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.ponto.CreateRegistroPonto;
import com.marcweb.dto.ponto.PontosRecentes;
import com.marcweb.dto.ponto.RegistroPontoResponse;
import com.marcweb.dto.ponto.StatusAtualFuncionario;
import com.marcweb.dto.TimeAdjustmentRequest;
import com.marcweb.dto.ponto.RegistroPontoMe;
import com.marcweb.entity.*;
import com.marcweb.exceptions.*;
import com.marcweb.repository.FeriadoRepository; // Import necessário
import com.marcweb.repository.RegistroPontoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek; // Import necessário
import java.time.LocalDate; // Import necessário
import java.time.LocalDateTime; // Import necessário
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RequiredArgsConstructor
@Service
public class RegistroPontoService {

    private final UserService userService;
    private final RegistroPontoRepository repository;
    private final ConfigEmpresaService configEmpresaService;
    private final FeriadoRepository feriadoRepository;
    private final FolgaService folgaService;
    private final Detalhes detalhes;


    @Auditado(
            acao = "Registro de ponto",
            categoria = CategoriaLog.PONTO)
    @Transactional
    public void registrarPonto(CreateRegistroPonto data, HttpServletRequest request) {

        // COLETANDO O USUÁRIO NO CONTEXTO DE SEGURANÇA
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userService.findByUsername(userDetails.getUsername());

        var dataAtual = LocalDate.now();


        if (data.getTipo().equals(TipoRegistro.ENTRADA)) {
            /// VERIFICANDO SE USUÁRIO ESTÁ DE FOLGA
            if (folgaService.existsByUserAndDate(user.getId(), dataAtual))
                throw new RegistroEmFolgaException();
            ///  BLOQUEANDO NOVO CICLO CASO A ULTIMA SAIDA TENHA SIDO HÁ MENOS DE 8 HORAS
            validarNovoCiclo(user);
        }

        // VALIDAÇÃO DE DIA ÚTIL (Novo passo - deve vir antes de outras validações)
        // A validação é feita com base no momento atual do servidor
        validarDiaUtil(dataAtual); // <-- Valida se é dia útil (não domingo e não feriado)

        // VALIDAÇÃO DE GEOLOCALIZAÇÃO (Novo passo)
        ConfigEmpresa config = configEmpresaService.findConfigsEmpresa().toEntity(); // Obtém a configuração ativa
        validarGeolocalizacao(data, config); // Chama o método de validação

        // VALIDAÇÃO DE REGISTRO (Sequência)
        validarRegistro(data.getTipo(), user.getId());


        ///  CAMPO QUE FICARÁ RESPONSÁVEL POR IDENTIFICAR UM DIA DE TRABALHO DE FOMRA INDIVIDUAL
        UUID expedienteId;

        /// GERANDO UM NOVO CAO SEJA PONTO DE ENTRADO
        if (data.getTipo().equals(TipoRegistro.ENTRADA)) {
            expedienteId = UUID.randomUUID();
        } else { /// PEGANDO O 'dataId' DO ULTIMO REGISTRO CASO CONTRÁRIO
            expedienteId = repository.findLastExpedienteIdByUserId(user.getId());
        }
        // SALVANDO CASO PASSE NAS VALIDAÇÕES
        // O RegistroPonto deve receber os dados do DTO e o usuário, o timestamp é definido internamente
        repository.save(new RegistroPonto(data, user, expedienteId, getIpUser(request))); // Assumindo que o construtor da entidade ou o JPA lida com o timestamp
        detalhes.setDetalhes("Usuário registrou ponto de " + data.getTipo().getText());
    }


    // VALIDANDO DIA ÚTIL
    private void validarDiaUtil(LocalDate momentoRegistro) { // Recebe LocalDateTime

        // Verifica se é domingo (DayOfWeek.SUNDAY)
        if (momentoRegistro.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RegistroEmDomingoException("Não é permitido registrar ponto no domingo.");
        }

        // Verifica se é feriado cadastrado
        // findByData retorna List<Feriado>. Se a lista não estiver vazia, é feriado.
        List<Feriado> feriadosNaData = feriadoRepository.findByData(momentoRegistro);
        if (!feriadosNaData.isEmpty()) { // Se a lista NÃO está vazia, encontrou-se um feriado
            // Pode usar o primeiro feriado encontrado para a mensagem
            Feriado feriado = feriadosNaData.get(0);
            throw new RegistroEmFeriadoException(String.format("Não é permitido registrar ponto em feriado (%s).", feriado.getNome()));
        }

        // Se passar pelas verificações, é um dia útil (não domingo e não feriado)
    }


    // VALIDANDO GEOLOCALIZAÇÃO
    private void validarGeolocalizacao(CreateRegistroPonto data, ConfigEmpresa config) {
        // Verifica se a geolocalização está ativa nas configurações
        if (Boolean.TRUE.equals(config.getGeolocAtiva())) { // Usando Boolean.TRUE.equals para evitar NPE se for null
            Double lat = data.getLatitude();
            Double lon = data.getLongitude();

            // Verifica se latitude e longitude foram fornecidas
            if (lat == null || lon == null) {
                throw new GeolocalizacaoObrigatoriaException("Geolocalização é obrigatória para registrar ponto. Latitude e longitude não fornecidas.");
            }

            // Verifica se as coordenadas da empresa estão configuradas
            Double latEmpresa = config.getLatCentro();
            Double lonEmpresa = config.getLongCentro();
            if (latEmpresa == null || lonEmpresa == null) {
                // Poderia lançar uma exceção específica de configuração inválida ou retornar um erro amigável
                // Para este caso, lançamos uma exceção informando que a configuração está incompleta.
                throw new IllegalStateException("Coordenadas da empresa não configuradas. Contate o administrador.");
            }

            // Calcula a distância
            double distancia = calcularDistanciaHaversine(lat, lon, latEmpresa, lonEmpresa);

            // Verifica se a distância está dentro do raio permitido
            if (distancia > config.getRaioGeoloc()) {
                throw new RegistroForaDoRaioException(
                        String.format("Registro fora do raio permitido. Distância calculada: %.2f metros. Raio permitido: %d metros.",
                                distancia, config.getRaioGeoloc())
                );
            }
        }
        // Se geolocAtiva for false (ou null), a validação é ignorada e o método termina aqui.
    }


    // CÁLCULO DE DISTÂNCIA USANDO A FÓRMULA DE HAVERSINE
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em quilômetros
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInKm = R * c; // Distância em quilômetros
        return distanceInKm * 1000; // Converte para metros
    }


    // VERIFICAR SE EXISTEM REGISTROS INCOMPLETOS DE DIAS ANTERIORES
    public boolean temRegistroIncompletoAnterior(Long userId) {
        List<RegistroPonto> registrosAnteriores = repository.findLastRecordsFromPreviousDays(userId);
        if (!registrosAnteriores.isEmpty()) {
            RegistroPonto ultimoRegistro = registrosAnteriores.get(0);
            // Verificar se o último registro não é SAIDA (ou seja, o dia não foi finalizado corretamente)
            return !ultimoRegistro.getTipo().equals(TipoRegistro.SAIDA);
        }
        return false;
    }

    // VALIDANDO ORDEM DE TIPO DE REGISTRO DE PONTO
    private void validarRegistro(TipoRegistro tipoRegistro, Long idUsuario) {
        /// BUSCANDO ULTIMO REGISTRO DO DIA NO BANCO DE DADOS
        Optional<TipoRegistro> possivelUltimoTipoRegistro = repository.findLastTypeRegistration(idUsuario, LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));

        // VERIFICAÇÃO PARA O PRIMEIRO REGISTRO DO USUÁRIO
        if (possivelUltimoTipoRegistro.isEmpty()) {
            // O primeiro registro DEVE ser uma ENTRADA
            if (tipoRegistro != TipoRegistro.ENTRADA) {
                // Lança exceção informando que o primeiro registro deve ser uma entrada
                throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.ENTRADA);
            }
            // Se for ENTRADA, é um registro válido, não precisa validar regras subsequentes
            return;
        }

        TipoRegistro ultimoTipoRegistro = possivelUltimoTipoRegistro.get();

        /* SE O ULTIMO REGISTRO DAQUELE DIA FOI O DE SAÍDA, CONSIDERA QYE O CICLO FOI FINALIZADO (PRECISA DE CORREÇÃO)
        if (ultimoTipoRegistro.equals(TipoRegistro.SAIDA))
            throw new CicloPontoFinalizadoException();*/

        // NOVA LÓGICA: VERIFICAR SE O ÚLTIMO REGISTRO É DE UM DIA ANTERIOR E NÃO FOI FINALIZADO (SAÍDA)
        if (tipoRegistro == TipoRegistro.ENTRADA && !ultimoTipoRegistro.equals(TipoRegistro.SAIDA)) {
            // Verificar se o último registro é de um dia anterior
            List<RegistroPonto> registrosAnteriores = repository.findLastRecordsFromPreviousDays(idUsuario);
            if (!registrosAnteriores.isEmpty()) {
                RegistroPonto ultimoRegistro = registrosAnteriores.get(0);
                // Se o último registro não é SAIDA e é de um dia anterior, permitir ENTRADA mas com aviso
                if (!ultimoRegistro.getTipo().equals(TipoRegistro.SAIDA)) {
                    // Permitir o registro de ENTRADA, mas o sistema deverá notificar o usuário
                    // que ele precisa contatar o supervisor para ajustar o dia anterior
                    return; // Permite a entrada, o aviso será tratado no frontend
                }
            }
        }

        // VALIDAÇÃO DE ORDEM PARA REGISTROS SUBSEQUENTES (código original ajustado)
        if (ultimoTipoRegistro.equals(TipoRegistro.ENTRADA) && !tipoRegistro.equals(TipoRegistro.INICIO_DESCANSO))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.INICIO_DESCANSO);

        if (ultimoTipoRegistro.equals(TipoRegistro.INICIO_DESCANSO) && !tipoRegistro.equals(TipoRegistro.FIM_DESCANSO))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.FIM_DESCANSO);

        if (ultimoTipoRegistro.equals(TipoRegistro.FIM_DESCANSO) && !tipoRegistro.equals(TipoRegistro.SAIDA))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.SAIDA);

        if (ultimoTipoRegistro.equals(TipoRegistro.SAIDA) && !tipoRegistro.equals(TipoRegistro.ENTRADA))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.ENTRADA);
    }


    @Auditado(
            acao = "Buscando registros de ponto",
            categoria = CategoriaLog.ADMIN)
    @Transactional
    public RegistroPontoMe buscarPontosRegistrados(LocalDateTime start, LocalDateTime end) {
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userService.findByUsername(userDetails.getUsername());
        detalhes.setDetalhes("Usuário realizou a busca dos seus pontos batidos entre " + start.toLocalDate().toString() + " e " + end.toLocalDate().toString());
        /// REGISTROS QUE FAZEM PARTE DO ULTIMO EXPEDIENTE ENCONTRADO
        ///  QUE PORÉM NÃO VIERAM NA QUERY POR TEREM SIDO FINALIZADAS NO MÊS SEGUINTE
        List<RegistroPontoResponse> registros = repository.findByUserAndTimestampBetween(user, start, end);
        if (!registros.isEmpty()) {
            var registrosFaltantes = completarLista(registros.get(registros.size() - 1));
            registros.addAll(registrosFaltantes);
            /// REMOVENDO OS PRIMEIROS REGISTROS ATÉ ENCONTRAR O PRIMEIRO PONTO DE 'ENTRADA' ENCONTRADO
            for (int i = 0; i < 3; i++) {
                if (!registros.isEmpty() && !registros.get(0).getTipo().equals(TipoRegistro.ENTRADA))
                    registros.remove(0);
                else break;
            }
            registros.sort(Comparator.comparing(RegistroPontoResponse::getTimestamp));
        }
        RegistroPontoMe meusRegistros = new RegistroPontoMe(registros, calcularHoras(registros), start.getMonth().length(start.toLocalDate().isLeapYear()));
        return meusRegistros;
    }

    /// USADO PARA BUSCAR REGISTROS QUE NÃO VIERAM NA PRIMEIRA CONSULTA
    /// POR CONTA DO EXPEDIENTE TER SIDO FINALIZADO NO MÊS SEGUINTE
    private List<RegistroPontoResponse> completarLista(RegistroPontoResponse ultimoRegistro) {
        if (!ultimoRegistro.getTipo().equals(TipoRegistro.SAIDA)) {
            List<RegistroPonto> registrosFaltantes = findByExpedienteIdAndTimestampGreaterThan(ultimoRegistro.getIdExpediente(), ultimoRegistro.getTimestamp());
            List<RegistroPontoResponse> resgistrosResponse = registrosFaltantes.stream().map(r ->
                            new RegistroPontoResponse(r.getTipo(), r.getUser().getNome(), r.getUser().getUsername(), r.getIp(), r.getJustificativa(), r.getTimestamp(), r.getExpedienteId()))
                    .toList();
            return resgistrosResponse;
        }
        return new ArrayList<>();
    }

    private long calcularHoras(List<RegistroPontoResponse> registros) {
        long minutosTrabalhados = 0;

        /// PARA SEPARAR REGISTROS POR DIA
        List<List<RegistroPontoResponse>> registrosSeparados = new ArrayList<>();
        List<RegistroPontoResponse> listaAux = new ArrayList<>();
        for (int i = 0; i < registros.size(); i++) {
            var registroAtual = registros.get(i);
            listaAux.add(registroAtual);
            var dataRegistroAtual = registroAtual.getTimestamp().toLocalDate();
            /// PARANDO O LOOP CASO SEJA O ULTIMO REGISTRO PARA NÃO DAR ERRO
            if (i == registros.size() - 1) {
                registrosSeparados.add(listaAux);
                break;
            }

            /// FINALIZANDO A LISTA DO DIA CASO O ID_EXPEDIENTE DO PRÓXIMO REGISTRO SEJA DIFERENTE
            if (!registros.get(i + 1).getIdExpediente().equals(registroAtual.getIdExpediente())) {
                registrosSeparados.add(listaAux);
                listaAux = new ArrayList<>();
            }
        }

        for (List<RegistroPontoResponse> r : registrosSeparados) {
            try {
                var primeiroRegistro = r.get(0);
                var segundoRegistro = r.get(1);
                if (primeiroRegistro.getTipo().equals(TipoRegistro.ENTRADA) && segundoRegistro.getTipo().equals(TipoRegistro.INICIO_DESCANSO)) {
                    minutosTrabalhados += ChronoUnit.MINUTES.between(primeiroRegistro.getTimestamp(), segundoRegistro.getTimestamp());
                }
            } catch (Exception e) {
                System.out.println("NÃO FOI POSSÍVEL CALCULAR COM OS REGISTROS ATUAIS");
            }
            try {
                var terceiroRegistro = r.get(2);
                var quartoRegistro = r.get(3);
                if (terceiroRegistro.getTipo().equals(TipoRegistro.FIM_DESCANSO) && quartoRegistro.getTipo().equals(TipoRegistro.SAIDA)) {
                    minutosTrabalhados += ChronoUnit.MINUTES.between(terceiroRegistro.getTimestamp(), quartoRegistro.getTimestamp());
                }
            } catch (Exception e) {
                System.out.println("NÃO FOI POSSÍVEL CALCULAR COM OS REGISTROS ATUAIS");
            }
        }
        return minutosTrabalhados;
    }


    /// COLETANDO IP DO USUÁRIO ATRAVÉZ DA REQUISIÇÃO
    private String getIpUser(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        else ip = ip.split(",")[0];
        return ip;
    }


    /// BSCANDO PONTOS DE UM USUÁRIO BATIDOS EM UM MES DE UM ANO ESPECIFICO
    public List<RegistroPonto> findByMesAno(Long userId, int mes, int ano) {
        // PRIMEIRO DIA DO MÊS                      ANO - MES - DIA DO MES - HORA - MINUTO
        LocalDateTime inicioMes = LocalDateTime.of(ano, mes, 1, 0, 0);

        // ULTIMO DIA DO MES VERIFICANDO SE O ANO É BISSEXTO OU NÃO
        LocalDateTime finalMes = inicioMes.plusDays(inicioMes.getMonth().length(inicioMes.toLocalDate().isLeapYear()));
        // USANDO O ID PARA REFERENCIAR A CHAVE ESTRANGEIRA
        var user = User.builder().id(userId).build();
        return repository.buscarRegistrosEmIntervaloDeData(
                user, inicioMes, finalMes
        );
    }


    public List<PontosRecentes> getStatusRecentes(LocalDate date) {
        var userDetails = UserService.getUserDetailsBySecurityContext();
        var user = userService.findByUsername(userDetails.getUsername());
        ///  BUSCANDO REGISTROS DE UMA DATA ESPECÍFICA EM QUALQUER HORÁRIO DO DIA
        List<RegistroPonto> pontosRecentesEntity = repository.buscarRegistrosEmIntervaloDeData(user, date.atStartOfDay(), date.atTime(LocalTime.MAX));
        List<PontosRecentes> pontosRecentesDTO = pontosRecentesEntity.stream().map(r -> {
            String horario = r.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm"));
            return new PontosRecentes(r.getTipo().getText(), horario);
        }).toList();
        return pontosRecentesDTO;
    }

    public List<StatusAtualFuncionario> getStatusAtualFuncionarios() {
        List<Object[]> results = repository.getStatusAtualFuncionarios();
        return results.stream().map(row -> {
            String nome = (String) row[0];
            String tipoStr = (String) row[1];
            String status = tipoStr.equals("Falta") ? "Falta" : TipoRegistro.valueOf(tipoStr).getText();
            String horarioEntrada = (String) row[2];
            Integer emServicoInt = ((Number) row[3]).intValue();
            Boolean emServico = emServicoInt == 1;
            return new StatusAtualFuncionario(nome, status, horarioEntrada, emServico);
        }).toList();
    }

    public List<RegistroPontoResponse> getAjustesAdministrativos(LocalDateTime start, LocalDateTime end) {
        List<RegistroPonto> ajustes = repository.findByIpAndTimestampBetweenOrderByTimestampDesc("AJUSTE_ADMIN", start, end);
        return ajustes.stream().map(r -> {
            return new RegistroPontoResponse(
                    r.getTipo(),
                    r.getUser().getNome(), // Nome do usuário
                    r.getUser().getUsername(),
                    r.getIp(),
                    r.getJustificativa(),
                    r.getTimestamp(),
                    r.getExpedienteId());
        }).toList();
    }

    @Auditado(
            acao = "Ajuste de ponto manual",
            categoria = CategoriaLog.ADMIN
    )
    @Transactional
    public void ajustarPonto(TimeAdjustmentRequest request) {
        // Buscar o usuário
        User user = userService.findById(request.userId());

        // Validar o tipo de registro
        TipoRegistro tipoRegistro = mapTipoStringToEnum(request.tipo());

        // Criar o timestamp combinando data e hora
        LocalDateTime timestamp = request.date().atTime(request.time());

        /*
         * Buscando registro que tenha o mesmo tipo para o dia informado
         * caso tenha, atualiza, se não, cria um objeto sem argumentos
         * para ser persistido no banco de dados
         * */
        var possivelRegistro = repository.findByUserAndTipoAndTimestampBetween(user, tipoRegistro, request.date().atStartOfDay(), request.date().atTime(LocalTime.MAX))
                .orElse(new RegistroPonto());

        /// SÓ É NECESSÁRIO CASO SEJA UM NOVO REGISTRO
        if (possivelRegistro.getId() == null) {
            possivelRegistro.setUser(user);
            possivelRegistro.setTipo(tipoRegistro);
            possivelRegistro.setExpedienteId(UUID.randomUUID());
        }
        possivelRegistro.setTimestamp(timestamp);
        possivelRegistro.setJustificativa(request.justificativa());
        possivelRegistro.setIp("AJUSTE_ADMIN"); // Marcar como ajuste administrativo

        repository.save(possivelRegistro);
        detalhes.setDetalhes("Ajuste de ponto realizado para " + user.getNome() + ": " + tipoRegistro.getText() + " em " + timestamp);
    }

    // Método auxiliar para mapear string para TipoRegistro
    private TipoRegistro mapTipoStringToEnum(String tipoString) {
        return switch (tipoString.toLowerCase()) {
            case "entrada" -> TipoRegistro.ENTRADA;
            case "início descanso" -> TipoRegistro.INICIO_DESCANSO;
            case "fim descanso" -> TipoRegistro.FIM_DESCANSO;
            case "saída" -> TipoRegistro.SAIDA;
            default -> throw new IllegalArgumentException("Tipo de registro inválido: " + tipoString);
        };
    }

    public List<RegistroPonto> findByExpedienteIdAndTimestampGreaterThan(UUID expedienteId, LocalDateTime timestamp) {
        return repository.findByExpedienteIdAndTimestampGreaterThan(expedienteId, timestamp);
    }


    /// BLOQUEANDO NOVO REGISTRO CASO UM EXPEDIENTE TENHA SIDO FINALIZADO HÁ MENOS DE 8 HORAS ATRAS
    private void validarNovoCiclo(User user) {
        var registro = repository.findByUserIdAndTipoOrderByTimestampDesc(user.getId());
        var horasPassadas = ChronoUnit.HOURS.between(registro.getTimestamp(), LocalDateTime.now());
        if (horasPassadas < 8) throw new CicloPontoFinalizadoException();

    }

}
