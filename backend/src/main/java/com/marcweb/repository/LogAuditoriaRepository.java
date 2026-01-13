package com.marcweb.repository;

import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findByUserAndTimestampBetweenOrderByTimestampDesc(User user, LocalDateTime start, LocalDateTime end);
    List<LogAuditoria> findByUserId(Long userId);
    List<LogAuditoria> findByTimestampBetween(LocalDateTime inicio, LocalDateTime fim);
    List<LogAuditoria> findByCategoria(CategoriaLog categoria);
    List<LogAuditoria> findByStatus(StatusLog status);
}
