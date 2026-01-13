package com.marcweb.repository;

import com.marcweb.dto.LogAuditoriaResponse;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findByUserAndTimestampBetweenOrderByTimestampDesc(User user, LocalDateTime start, LocalDateTime end);

    @Query("Select new com.marcweb.dto.LogAuditoriaResponse(l) from LogAuditoria l where l.user.id = :userId ")
    List<LogAuditoriaResponse> findByUserId(@Param("userId") Long userId);

    @Query("Select new com.marcweb.dto.LogAuditoriaResponse(l) from LogAuditoria l where l.timestamp between :inicio and :fim")
    List<LogAuditoriaResponse> findByTimestampBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("Select new com.marcweb.dto.LogAuditoriaResponse(l) from LogAuditoria l where l.categoria = :categoria")
    List<LogAuditoriaResponse> findByCategoria(@Param("categoria") CategoriaLog categoria);

    @Query("Select new com.marcweb.dto.LogAuditoriaResponse(l) from LogAuditoria l where l.status = :status ")
    List<LogAuditoriaResponse> findByStatus(@Param("status") StatusLog status);
}
