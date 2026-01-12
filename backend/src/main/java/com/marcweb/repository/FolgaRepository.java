package com.marcweb.repository;

import com.marcweb.entity.Folga;
import com.marcweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FolgaRepository extends JpaRepository<Folga, Long> {

    // Método para encontrar folgas de um usuário específico usando ID (mais eficiente)
    List<Folga> findByUserId(Long userId);

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
    boolean existsByUserIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
            Long idUser, LocalDate dataInicio, LocalDate dataFim);


    // BUSCANDO POR PERIODO ESPECIFICO
    List<Folga> findByUserIdAndDataInicioGreaterThanEqualAndDataFimLessThanEqual(
            Long idUser, LocalDate dataInicio, LocalDate dataFim);

    List<Folga> findByUserAndDataInicioBetweenOrUserAndDataFimBetween(
            User user, LocalDate start1, LocalDate end1, User user2, LocalDate start2, LocalDate end2);


    // Outros métodos específicos, se necessário
}