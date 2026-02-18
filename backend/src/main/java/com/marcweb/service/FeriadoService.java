package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.feriado.CreateFeriado;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.Feriado;
import com.marcweb.entity.OrigemFeriado;
import com.marcweb.exceptions.FeriadoDuplicadoException;
import com.marcweb.repository.FeriadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class FeriadoService {

    private final FeriadoRepository repository;
    private final Detalhes detalhes;


    // CADASTRO MANUAL
    @Auditado(
            acao = "Registro de feriado",
            categoria = CategoriaLog.ADMIN)
    public void cadastrarFeriado(CreateFeriado dadosFeriado) {
        var feriadoExiste = validarFeriado(dadosFeriado.getData());
        if (feriadoExiste) throw new FeriadoDuplicadoException();
        repository.save(new Feriado(dadosFeriado, OrigemFeriado.MANUAL));
        detalhes.setDetalhes("Administrador registrou o feriado de " + dadosFeriado.getNome() + " para o dia " + dadosFeriado.getData().toString());
    }

    // CADASTRO VIA API - Atualizado para retornar estatísticas
    public Map<String, Integer> cadastrarFeriadoComEstatisticas(List<Feriado> feriadosNacionais) {
        int totalBuscados = feriadosNacionais.size();
        int jaExistiam = 0; // Contador efetivamente final
        int adicionados = 0; // Contador para novos registros

        // Lista para armazenar apenas os feriados que não existiam
        List<Feriado> feriadosNovos = new ArrayList<>();

        // Itera manualmente para contabilizar e filtrar
        for (Feriado feriado : feriadosNacionais) {
            if (validarFeriado(feriado.getData())) { // isRequest = false para sincronização
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


    private boolean validarFeriado(LocalDate data) {
        return repository.existsByData(data);
    }

    @Auditado(
            acao = "Busca por feriados dentro de um periodo",
            categoria = CategoriaLog.AUDITORIA)
    public List<Feriado> findByDataBetween(LocalDate start, LocalDate end) {
        detalhes.setDetalhes("Usuário executou a busca por feriados entre " + start.toString() + " e " + end.toString());
        return repository.findByDataBetweenOrderByDataAsc(start, end);
    }


    public List<Feriado> findByAno(int ano) {
        /// PRIMEIRO DIA DO ANO SOLICITADO
        LocalDate start = LocalDate.ofYearDay(ano, 1);

        /// ULTIMO DIA DO ANO SOLICITADO
        LocalDate end = start.withDayOfYear(start.lengthOfYear());
        return repository.findByDataBetweenOrderByDataAsc(start, end);
    }

    @Auditado(
            acao = "Apagando feriado",
            categoria = CategoriaLog.ADMIN)
    public void deleteById(Long id) {
        var feriado = repository.findById(id).orElseThrow(() -> new RuntimeException("Feriado não encontrado"));
        repository.deleteById(id);
        detalhes.setDetalhes("Administrador removeu o feriado " + feriado.getNome() + " que estava registrado para o dia " + feriado.getData().toString());
    }


    @Auditado(
            acao = "Atualizando feriados para o ano vigente",
            categoria = CategoriaLog.ADMIN)
    public void atualizarFeriados() {

        // PRIMEIRO DIA DO ANO PASSADO
        LocalDate start = LocalDate.now().plusYears(-1).withDayOfYear(1);

        // ULTIMO DIA DO ANO PASSADO
        LocalDate end = start.withDayOfYear(start.lengthOfYear());

        /// BUSCANDO  FERIADOS DO ANO PASSADO. (SEM ID PARA SALVAR NOVOS REGISTROS E NÃO ATUALIZAR OS ENCONTRADOS)
        List<Feriado> feriadosManuais = repository.buscarRegistrosManuaisSemId(start, end);
        feriadosManuais.removeIf(feriado -> {
            feriado.setData(feriado.getData().plusYears(1));
            return validarFeriado(feriado.getData());
        });
        repository.saveAll(feriadosManuais);
        detalhes.setDetalhes("Atualizando todos os feriados manuais  para o ano vigente:  " + start.getYear());
    }


}