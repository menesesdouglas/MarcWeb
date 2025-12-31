package com.marcweb.repository;

import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {
    List<RegistroPonto> findByUserAndTimestampBetweenOrderByTimestampAsc(User user, LocalDateTime start, LocalDateTime end);
    // Outros métodos específicos, se necessário
}