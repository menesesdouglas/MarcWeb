package com.marcweb.service;

import com.marcweb.dto.CreateRegistroPonto;
import com.marcweb.dto.RegistroPontoResponse;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.entity.Feriado; // Import necessário
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
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
import java.util.List;

@RequiredArgsConstructor
@Service
public class RegistroPontoService {

    private final UserService userService;
    private final RegistroPontoRepository repository;
    private final ConfigEmpresaService configEmpresaService; // Injetando o serviço de configuração
    private final FeriadoRepository feriadoRepository; // Injetando o repositório de feriados
    private final FolgaService folgaService;
    private final LogAuditoriaService logAuditoriaService;

    @Transactional
    public void registrarPonto(CreateRegistroPonto data, HttpServletRequest request) {
        // COLETANDO O USUÁRIO NO CONTEXTO DE SEGURANÇA
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userService.findByUsername(userDetails.getUsername());

        var dataAtual = LocalDate.now();

        // VERIFICANDO SE USUÁRIO ESTÁ DE FOLGA
        // **** TROCAR A EXCEPTION DEPOIS *****
        if(folgaService.existsByUserAndDate(user.getId(), dataAtual)) throw new RegistroEmFolgaException();

        // VALIDAÇÃO DE DIA ÚTIL (Novo passo - deve vir antes de outras validações)
        // A validação é feita com base no momento atual do servidor
        validarDiaUtil(dataAtual); // <-- Usando LocalDateTime.now()

        // VALIDAÇÃO DE GEOLOCALIZAÇÃO (Novo passo)
        ConfigEmpresa config = configEmpresaService.findConfigsEmpresa().toEntity(); // Obtém a configuração ativa
        validarGeolocalizacao(data, config); // Chama o método de validação

        // VALIDAÇÃO DE REGISTRO (Sequência)
        validarRegistro(data.getTipo(), user.getId());

        // SALVANDO CASO PASSE NAS VALIDAÇÕES
        // O RegistroPonto deve receber os dados do DTO e o usuário, o timestamp é definido internamente
        repository.save(new RegistroPonto(data, user, getIpUser(request))); // Assumindo que o construtor da entidade ou o JPA lida com o timestamp
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


    // VALIDANDO ORDEM DE TIPO DE REGISTRO DE PONTO
    private void validarRegistro(TipoRegistro tipoRegistro, Long idUsuario) {
        // BUSCANDO ULTIMO REGISTRO NO BANCO DE DADOS
        TipoRegistro ultimoTipoRegistro = repository.findLastTypeRegistration(idUsuario);

        // VERIFICAÇÃO PARA O PRIMEIRO REGISTRO DO USUÁRIO
        if (ultimoTipoRegistro == null) {
            // O primeiro registro DEVE ser uma ENTRADA
            if (tipoRegistro != TipoRegistro.ENTRADA) {
                // Lança exceção informando que o primeiro registro deve ser uma entrada
                throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.ENTRADA);
            }
            // Se for ENTRADA, é um registro válido, não precisa validar regras subsequentes
            return;
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

    public List<RegistroPontoResponse> buscarPontosRegistrados(LocalDateTime start, LocalDateTime end) {
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userService.findByUsername(userDetails.getUsername());
        return repository.findByUserAndTimestampBetweenOrderByTimestampAsc(user, start, end);
    }

    // COLETANDO IP DO USUÁRIO ATRAVÉZ DA REQUISIÇÃO
    private String getIpUser(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        else ip = ip.split(",")[0];
        return ip;
    }


    // BSCANDO PONTOS DE UM USUÁRIO BATIDOS EM UM MES DE UM ANO ESPECIFICO
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
}
