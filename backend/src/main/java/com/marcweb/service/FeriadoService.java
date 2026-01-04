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

    // CADASTRO VIA API
    public void cadastrarFeriado(List<Feriado> feriadosNacionais) {
        // REMOVENDO FERIADOS CASO JA ESTEJAM CADASTRADOS
        feriadosNacionais.removeIf(feriado -> {
                    return validarFeriado(feriado.getData(), true);
                }
        );
        repository.saveAll(feriadosNacionais);
    }

    private boolean validarFeriado(LocalDate data, boolean isRequest) {
        // PARA VALIDAR QUE O FERIADO É DO ANO VIGENTE
        int anoFeriado = data.getYear();
        int anoAtual = Year.now().getValue();
        // VALIDAÇÃO APENAS PARA REQUISIÇÕES
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
