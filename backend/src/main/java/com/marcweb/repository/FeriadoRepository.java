package com.marcweb.repository;

import com.marcweb.entity.Feriado;
import com.marcweb.entity.OrigemFeriado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
    List<Feriado> findByDataBetweenOrderByDataAsc(LocalDate startDate, LocalDate endDate);

    List<Feriado> findByData(LocalDate date); // Para verificar se uma data específica é feriado
    // Outros métodos específicos, se necessário

    boolean existsByData(LocalDate data); // VERIFICA SE JÁ EXISTE FERIADO PARA UMA DATA


    @Query("Select new Feriado(f.data, f.nome, f.origem) from Feriado f where f.origem = MANUAL and data between :start and :end")
    List<Feriado> buscarRegistrosManuaisSemId(@Param("start") LocalDate start, @Param("end") LocalDate end);
}