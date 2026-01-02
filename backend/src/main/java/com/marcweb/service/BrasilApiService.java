package com.marcweb.service;

import com.marcweb.dto.FeriadoDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class BrasilApiService {

    private final WebClient webClient = WebClient.create("https://brasilapi.com.br/api");

    public List<FeriadoDTO> buscarFeriados(int ano) {
        return webClient.get()
                .uri("/feriados/v1/{ano}", ano)
                .retrieve()
                .bodyToFlux(FeriadoDTO.class)
                .collectList()
                .block();
    }

}
