package com.marcweb.repository;

import com.marcweb.entity.Folga;
import com.marcweb.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FolgaRepository extends JpaRepository<Folga, Long> {



    @Query("SELECT f FROM Folga f WHERE f.aprovada IS NULL")
    List<Folga> findFolgasPendentes();

    // Método para encontrar folgas de um usuário específico usando ID (mais eficiente)
    List<Folga> findByUserId(Long userId);

    Page<Folga> findByUserId(Long userId, Pageable pageable);

    // Método para encontrar folgas de um usuário (objeto) - também pode ser útil
    List<Folga> findByUser(User user);

    // Método customizado com @Query para verificar sobreposição excluindo um ID específico
    @Query("SELECT f FROM Folga f WHERE f.user.id = :userId AND " +
           ":dataFim >= f.dataInicio AND :dataInicio <= f.dataFim " +
           "AND (:folgaId IS NULL OR f.id != :folgaId)")
    List<Folga> findByUserAndPeriodoSobrepostoExcluindoId(
            @Param("userId") Long userId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("folgaId") Long folgaId); // Pode ser null para novos registros


    // UTILIZADO PARA SABER SE EXISTE UMA FOLGA EM UM CERTO INTERVALO
    // USADO PARA VALIDAR SE EXISTE FOLGA NO DIA EM QUE UM PONTO TENTA SER BATIDO
    boolean existsByUserIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqualAndAprovada(
            Long idUser, LocalDate dataInicio, LocalDate dataFim, boolean aprovada);


    /// BUSCANDO FOLGAS  POR PERIODO ESPECIFICO
    List<Folga> findByUserIdAndDataInicioGreaterThanEqualAndDataFimLessThanEqualAndAprovada(
            Long idUser, LocalDate dataInicio, LocalDate dataFim, boolean aprovada);

    List<Folga> findByUserAndDataInicioBetweenOrUserAndDataFimBetween(
            User user, LocalDate start1, LocalDate end1, User user2, LocalDate start2, LocalDate end2);


    // Outros métodos específicos, se necessário

    // CONSULTA OTIMIZADA: FOLGAS POR USUÁRIO COM PAGINAÇÃO
    @Query("SELECT f FROM Folga f WHERE f.user.id = :userId ORDER BY f.dataInicio DESC")
    List<Folga> findByUserIdWithPagination(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: FOLGAS POR USUÁRIO E PERÍODO COM PAGINAÇÃO
    @Query("SELECT f FROM Folga f WHERE f.user.id = :userId AND " +
           "(:dataInicio IS NULL OR f.dataFim >= :dataInicio) AND " +
           "(:dataFim IS NULL OR f.dataInicio <= :dataFim) " +
           "ORDER BY f.dataInicio DESC")
    List<Folga> findByUserIdAndPeriodoWithPagination(
            @Param("userId") Long userId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: CONTAGEM DE FOLGAS POR USUÁRIO
    @Query("SELECT COUNT(f) FROM Folga f WHERE f.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // CONSULTA OTIMIZADA: CONTAGEM DE FOLGAS POR USUÁRIO E PERÍODO
    @Query("SELECT COUNT(f) FROM Folga f WHERE f.user.id = :userId AND " +
           "f.dataInicio <= :dataFim AND f.dataFim >= :dataInicio")
    long countByUserIdAndPeriodo(
            @Param("userId") Long userId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);
}
