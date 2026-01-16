package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.dto.FeriadoNacionalDTO;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.Feriado;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Year;
import java.util.List;
import java.util.Map; // Import necessário
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BrasilApiService {

    // Corrigido URL base
    private final WebClient webClient = WebClient.create("https://brasilapi.com.br/api");

    private final FeriadoService feriadoService;


    // Método auxiliar para buscar feriados da API, recebe ano como parâmetro
    private List<FeriadoNacionalDTO> buscarFeriados(int ano) {
        return webClient.get()
                .uri("/feriados/v1/{ano}", ano) // Inclui o ano na URI
                .retrieve()
                .bodyToFlux(FeriadoNacionalDTO.class)
                .collectList()
                .block();
    }

    // Método para sincronizar feriados de um ano específico, retorna estatísticas
    @Auditado(
            acao = "Registro de feriados de um ano específico",
            categoria = CategoriaLog.ADMIN,
            detalhes = "Administrador executou o registro dos feriados nacionais de um ano específico"
    )
    public Map<String, Integer> sincronizarFeriados(int ano) {
        // Validação de ano pode ser adicionada aqui se necessário
        // if (ano < Year.now().getValue()) throw new IllegalArgumentException(...)

        List<FeriadoNacionalDTO> feriadoDTOList = buscarFeriados(ano);
        List<Feriado> feriadosNacionais = feriadoDTOList
                .stream()
                .map(Feriado::new) // Assumindo que o construtor Feriado(FeriadoNacionalDTO) existe
                .collect(Collectors.toList());

        // Chama o método atualizado do service que retorna estatísticas
        return feriadoService.cadastrarFeriadoComEstatisticas(feriadosNacionais);
    }

    //  ORDEM DO CRON ( SEGUNDO, MINUTO, HORA, DIA DO MES, MES, DIA DA SEMANA
    // EXECUTA TODO DIA 1° DE JANEIRO ÀS MEIA NOITE E DEZ MINUTOS
    @Auditado(
            acao = "Cadastrando feriados nacionais",
            categoria = CategoriaLog.ADMIN,
            detalhes = "Administrador executou o registro dos feriados nacionais do ano vigente"
    )
    @Scheduled(cron = "00 10 00 01 01 *", zone = "America/Sao_Paulo")
    public void cadastrarFeriadosNacionais() {
        // Chama o novo método com o ano atual
        sincronizarFeriados(Year.now().getValue());
        // O retorno (estatísticas) pode ser logado aqui se desejado
        // log.info("Sincronização automática de feriados para {}: {}", Year.now().getValue(), stats);
    }

}