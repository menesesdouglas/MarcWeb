package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.feriado.FeriadoNacionalDTO;
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

    private final Detalhes detalhes;


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
            categoria = CategoriaLog.ADMIN)
    public Map<String, Integer> sincronizarFeriados(int ano) {
        detalhes.setDetalhes("Administrador executou o registro dos feriados nacionais de um ano específico");
        List<FeriadoNacionalDTO> feriadoDTOList = buscarFeriados(ano);
        List<Feriado> feriadosNacionais = feriadoDTOList
                .stream()
                .map(Feriado::new)
                .collect(Collectors.toList());
        return feriadoService.cadastrarFeriadoComEstatisticas(feriadosNacionais);
    }


}