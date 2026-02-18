package com.marcweb.repository;

import com.marcweb.entity.Feriado;
import com.marcweb.entity.OrigemFeriado; // Import necessário
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

    boolean existsByData(LocalDate data); // VERIFICA SE JÁ EXISTE FERIADO PARA UMA DATA


    @Query("Select new Feriado(f.data, f.nome, f.origem) from Feriado f where f.origem = 'MANUAL' and f.data between :start and :end")
    List<Feriado> buscarRegistrosManuaisSemId(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // CONSULTA OTIMIZADA: FERIADOS COM PAGINAÇÃO
    @Query("SELECT f FROM Feriado f ORDER BY f.data")
    List<Feriado> findAllWithPagination(org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: FERIADOS POR ORIGEM
    @Query("SELECT f FROM Feriado f WHERE f.origem = :origem ORDER BY f.data")
    List<Feriado> findByOrigem(@Param("origem") OrigemFeriado origem);

    // CONSULTA OTIMIZADA: FERIADOS POR ORIGEM COM PAGINAÇÃO
    @Query("SELECT f FROM Feriado f WHERE f.origem = :origem ORDER BY f.data")
    List<Feriado> findByOrigemWithPagination(@Param("origem") OrigemFeriado origem, org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: CONTAGEM DE FERIADOS POR ORIGEM
    @Query("SELECT COUNT(f) FROM Feriado f WHERE f.origem = :origem")
    long countByOrigem(@Param("origem") OrigemFeriado origem);

    // CONSULTA OTIMIZADA: FERIADOS POR PERÍODO COM PAGINAÇÃO
    @Query("SELECT f FROM Feriado f WHERE f.data BETWEEN :start AND :end ORDER BY f.data")
    List<Feriado> findByPeriodoWithPagination(@Param("start") LocalDate start, @Param("end") LocalDate end, org.springframework.data.domain.Pageable pageable);

}
