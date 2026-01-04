package com.marcweb.service;

import com.marcweb.dto.FeriadoNacionalDTO;
import com.marcweb.entity.Feriado;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BrasilApiService {

    private final WebClient webClient = WebClient.create("https://brasilapi.com.br/api");

    private final FeriadoService feriadoService;

    private List<FeriadoNacionalDTO> buscarFeriados(int ano) {
        return webClient.get()
                .uri("/feriados/v1/{ano}", ano)
                .retrieve()
                .bodyToFlux(FeriadoNacionalDTO.class)
                .collectList()
                .block();
    }

    //  ORDEM DO CRON ( SEGUNDO, MINUTO, HORA, DIA DO MES, MES, DIA DA SEMANA
    // EXECUTA TODO DIA 1° DE JANEIRO ÀS MEIA NOITE E DEZ MINUTOS
    @Scheduled(cron = "00 10 00 01 01 *", zone = "America/Sao_Paulo")
    public void cadastrarFeriadosNacionais() {
        List<FeriadoNacionalDTO> feriadoDTOList = buscarFeriados(Year.now().getValue());
        List<Feriado> feriadosNacionais = feriadoDTOList
                .stream().map(Feriado::new).collect(Collectors.toList());
        feriadoService.cadastrarFeriado(feriadosNacionais);
    }

}
