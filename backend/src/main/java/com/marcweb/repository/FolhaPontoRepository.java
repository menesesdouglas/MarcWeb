package com.marcweb.repository;

import com.marcweb.entity.FolhaPonto;
import com.marcweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolhaPontoRepository extends JpaRepository<FolhaPonto, Long> {

    FolhaPonto findByUserIdAndMesAndAno(Long userId, int mes, int ano);

    // CONSULTA OTIMIZADA: FOLHAS DE PONTO POR USUÁRIO
    @Query("Select f from FolhaPonto f where f.user = :user order by f.ano desc, f.mes desc")
    List<FolhaPonto> findByUserOrderByDataDesc(@Param("user") User user);

    // CONSULTA OTIMIZADA: FOLHAS DE PONTO POR USUÁRIO E ANO
    @Query("Select f from FolhaPonto f where f.user = :user and f.ano = :ano order by f.mes desc")
    List<FolhaPonto> findByUserAndAnoOrderByMesDesc(@Param("user") User user, @Param("ano") int ano);

    // CONSULTA OTIMIZADA: CONTAGEM DE FOLHAS POR USUÁRIO
    @Query("Select count(f) from FolhaPonto f where f.user = :user")
    long countByUser(@Param("user") User user);
}
