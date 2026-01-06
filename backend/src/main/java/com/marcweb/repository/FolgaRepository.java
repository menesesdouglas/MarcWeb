package com.marcweb.repository;

import com.marcweb.entity.Folga;
import com.marcweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FolgaRepository extends JpaRepository<Folga, Long> {

    List<Folga> findByUserAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
        User user, LocalDate dataFim, LocalDate dataInicio);

    List<Folga> findByUserAndDataInicioBetweenOrUserAndDataFimBetween(
            User user, LocalDate start1, LocalDate end1, User user2, LocalDate start2, LocalDate end2);
    // Outros métodos específicos, se necessário
}