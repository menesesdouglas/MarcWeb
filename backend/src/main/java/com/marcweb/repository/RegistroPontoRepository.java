package com.marcweb.repository;

import com.marcweb.dto.ponto.RegistroPontoResponse;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    @Query("""
            Select new com.marcweb.dto.ponto.RegistroPontoResponse(r.tipo, r.user.nome, r.user.username, r.ip, r.justificativa, r.timestamp)
             from RegistroPonto r where r.user = :user and r.timestamp between :start and :end
            """)
    List<RegistroPontoResponse> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);


    // BUSCANDO REGISTROS DE UM USUÁRIO EM UM INTERVALO DE DATA
    @Query("""
            Select r from RegistroPonto r where r.user = :user and r.timestamp between :start and :end
            order by r.timestamp
            """)
    List<RegistroPonto> buscarRegistrosEmIntervaloDeData(User user, LocalDateTime start, LocalDateTime end);
    // Outros métodos específicos, se necessário

    // BUSCANDO ULTIMO TIPO DE REGISTRO DE UM USUÁRIO
    @Query(nativeQuery = true, value = """
            Select r.tipo from registros_ponto r where r.user_id = :idUsuario
            order by r.timestamp Desc limit 1
            """)
    TipoRegistro findLastTypeRegistration(@Param("idUsuario") Long idUsuario);

    // CONSULTA OTIMIZADA: REGISTROS DE PONTO COM PAGINAÇÃO
    @Query("""
            Select new com.marcweb.dto.ponto.RegistroPontoResponse(r.tipo, r.user.nome, r.user.username, r.ip, r.justificativa, r.timestamp)
            from RegistroPonto r where r.user = :user
            order by r.timestamp Desc
            """)
    List<RegistroPontoResponse> findByUserWithPagination(User user, org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: REGISTROS DE PONTO POR PERÍODO COM PAGINAÇÃO
    @Query("""
            Select new com.marcweb.dto.ponto.RegistroPontoResponse(r.tipo, r.user.nome, r.user.username, r.ip, r.justificativa, r.timestamp)
            from RegistroPonto r where r.user = :user and r.timestamp between :start and :end
            order by r.timestamp Desc
            """)
    List<RegistroPontoResponse> findByUserAndPeriodWithPagination(User user, LocalDateTime start, LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: CONTAGEM DE REGISTROS POR USUÁRIO
    @Query("Select count(r) from RegistroPonto r where r.user = :user")
    long countByUser(@Param("user") User user);

    // CONSULTA OTIMIZADA: CONTAGEM DE REGISTROS POR USUÁRIO E PERÍODO
    @Query("Select count(r) from RegistroPonto r where r.user = :user and r.timestamp between :start and :end")
    long countByUserAndPeriod(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query("""
            Select r from RegistroPonto r where r.user.id = :userId
            and r.timestamp < CURRENT_DATE
            order by r.timestamp DESC
            """)
    List<RegistroPonto> findLastRecordsFromPreviousDays(@Param("userId") Long userId);

    @Query(nativeQuery = true, value = """
            SELECT u.nome,
                   COALESCE(r.tipo, 'Falta') as status,
                   COALESCE(TO_CHAR(e.timestamp, 'HH24:MI'), '--:--') as horario_entrada,
                   CASE
                       WHEN r.tipo IS NULL THEN 0
                       WHEN r.tipo IN ('ENTRADA', 'FIM_DESCANSO') THEN 1
                       ELSE 0
                   END as em_servico
            FROM users u
            LEFT JOIN (
                SELECT rp.user_id, rp.tipo
                FROM registros_ponto rp
                WHERE DATE(rp.timestamp) = CURRENT_DATE
                AND rp.timestamp = (
                    SELECT MAX(rp2.timestamp)
                    FROM registros_ponto rp2
                    WHERE rp2.user_id = rp.user_id
                    AND DATE(rp2.timestamp) = CURRENT_DATE
                )
            ) r ON u.id = r.user_id
            LEFT JOIN (
                SELECT rp.user_id, rp.timestamp
                FROM registros_ponto rp
                WHERE DATE(rp.timestamp) = CURRENT_DATE
                AND rp.tipo = 'ENTRADA'
                AND rp.timestamp = (
                    SELECT MIN(rp2.timestamp)
                    FROM registros_ponto rp2
                    WHERE rp2.user_id = rp.user_id
                    AND DATE(rp2.timestamp) = CURRENT_DATE
                    AND rp2.tipo = 'ENTRADA'
                )
            ) e ON u.id = e.user_id
            WHERE u.ativo = true
            ORDER BY u.nome
            """)
    List<Object[]> getStatusAtualFuncionarios();

    // Método para verificar se já existe um registro no mesmo horário para evitar duplicatas
    Optional<RegistroPonto> findByUserAndTipoAndTimestampBetween(User user, TipoRegistro tipo, LocalDateTime start, LocalDateTime end);

    // Método para buscar ajustes administrativos por IP e período
    @Query("SELECT r FROM RegistroPonto r WHERE r.ip = :ip AND r.timestamp BETWEEN :start AND :end ORDER BY r.timestamp DESC")
    List<RegistroPonto> findByIpAndTimestampBetweenOrderByTimestampDesc(@Param("ip") String ip, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Método para obter o timestamp do último registro de um usuário
    @Query("SELECT r.timestamp FROM RegistroPonto r WHERE r.user.id = :userId ORDER BY r.timestamp DESC")
    LocalDateTime findLastTimestamp(@Param("userId") Long userId);
}
