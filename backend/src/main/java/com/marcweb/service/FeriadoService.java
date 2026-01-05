package com.marcweb.service;

import com.marcweb.dto.CreateFeriado;
import com.marcweb.entity.Feriado;
import com.marcweb.entity.OrigemFeriado;
import com.marcweb.exceptions.FeriadoAnoInvalidoException;
import com.marcweb.exceptions.FeriadoDuplicadoException;
import com.marcweb.repository.FeriadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.ArrayList; // Import necessário
import java.util.HashMap; // Import necessário
import java.util.Map;    // Import necessário

@RequiredArgsConstructor
@Service
public class FeriadoService {

    private final FeriadoRepository repository;


    // CADASTRO MANUAL
    public void cadastrarFeriado(CreateFeriado dadosFeriado) {
        var feriadoExiste = validarFeriado(dadosFeriado.getData(), true);
        if (feriadoExiste) throw new FeriadoDuplicadoException();
        repository.save(new Feriado(dadosFeriado, OrigemFeriado.MANUAL));
    }

    // CADASTRO VIA API - Atualizado para retornar estatísticas
    public Map<String, Integer> cadastrarFeriadoComEstatisticas(List<Feriado> feriadosNacionais) {
        int totalBuscados = feriadosNacionais.size();
        int jaExistiam = 0; // Contador efetivamente final (não modificado dentro de lambda)
        int adicionados = 0; // Contador para novos registros

        // Lista para armazenar apenas os feriados que não existiam
        List<Feriado> feriadosNovos = new ArrayList<>();

        // Itera manualmente para contabilizar e filtrar
        for (Feriado feriado : feriadosNacionais) {
            if (validarFeriado(feriado.getData(), false)) { // isRequest = false para sincronização
                jaExistiam++; // Conta os que já existiam
            } else {
                feriadosNovos.add(feriado); // Adiciona os novos à lista
            }
        }

        // Salva apenas os feriados novos
        if (!feriadosNovos.isEmpty()) {
            repository.saveAll(feriadosNovos);
            adicionados = feriadosNovos.size();
        }

        // Monta e retorna o mapa de estatísticas
        Map<String, Integer> estatisticas = new HashMap<>();
        estatisticas.put("totalBuscados", totalBuscados);
        estatisticas.put("jaExistiam", jaExistiam);
        estatisticas.put("adicionados", adicionados);

        return estatisticas;
    }


    private boolean validarFeriado(LocalDate data, boolean isRequest) {
        // PARA VALIDAR QUE O FERIADO É DO ANO VIGENTE
        int anoFeriado = data.getYear();
        int anoAtual = Year.now().getValue();
        // VALIDAÇÃO APENAS PARA REQUISIÇÕES MANUAIS (isRequest = true)
        if (isRequest && anoAtual != anoFeriado) {
            throw new FeriadoAnoInvalidoException();
        }
        return repository.existsByData(data);
    }

    public List<Feriado> findByDataBetween(LocalDate start, LocalDate end) {
        return repository.findByDataBetweenOrderByDataAsc(start, end);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }


    // ATUALIZANDO FERIADOS PARA O PRÓXIMO ANO
    // EXECUTA TODO DIA 1° DE JANEIRO ÀS MEIA NOITE E DEZ MINUTOS
    @Scheduled(cron = "00 10 00 01 01 *", zone = "America/Sao_Paulo")
    private void atualizarFeriados() {

        // PRIMEIRO DIA DO ANO
        LocalDate start = LocalDate.now().withDayOfYear(1);

        // ULTIMO DIA DO ANO
        LocalDate end = start.withDayOfYear(start.lengthOfYear());

        // BUSCANDO  FERIADOS DO ANO ATUAL. (SEM ID PARA SALVAR NOVOS REGISTROS E NÃO ATUALIZAR OS ENCONTRADOS)
        List<Feriado> feriadosManuais = repository.buscarRegistrosManuaisSemId(start, end);
        feriadosManuais.removeIf(feriado -> {
            feriado.setData(feriado.getData().plusYears(1));
            return validarFeriado(feriado.getData(), false);
        });
        repository.saveAll(feriadosManuais);
    }


}