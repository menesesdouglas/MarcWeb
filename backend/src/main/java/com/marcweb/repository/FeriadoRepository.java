package com.marcweb.repository;

import com.marcweb.entity.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
    List<Feriado> findByDataBetweenOrderByDataAsc(LocalDate startDate, LocalDate endDate);
    List<Feriado> findByData(LocalDate date); // Para verificar se uma data específica é feriado
    // Outros métodos específicos, se necessário



}