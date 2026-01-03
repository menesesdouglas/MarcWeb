package com.marcweb.service;

import com.marcweb.dto.CreateFeriado;
import com.marcweb.entity.OrigemFeriado;
import com.marcweb.repository.FeriadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class FeriadoService {

    private final FeriadoRepository repository;

    public void cadastrarFeriado(CreateFeriado data, OrigemFeriado origemFeriado) {

    }


    private void validarFeriado(CreateFeriado data) {
        // PRIMEIRO DIA DO ANO
        LocalDate start = LocalDate.now().withDayOfYear(1);

        // ULTIMO DIA DO ANO
        LocalDate end = LocalDate.now().withDayOfYear(start.lengthOfYear());


    }
}
