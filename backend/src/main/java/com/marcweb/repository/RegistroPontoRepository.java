package com.marcweb.repository;

import com.marcweb.dto.RegistroPontoResponse;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    @Query("""
            Select new com.marcweb.dto.RegistroPontoResponse(r.tipo, r.user.nome, r.user.username, r.ip, r.justificativa, r.timestamp)
             from RegistroPonto r where r.user = :user and r.timestamp between :start and :end
            """)
    List<RegistroPontoResponse> findByUserAndTimestampBetweenOrderByTimestampAsc(User user, LocalDateTime start, LocalDateTime end);


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
            Select new com.marcweb.dto.RegistroPontoResponse(r.tipo, r.user.nome, r.user.username, r.ip, r.justificativa, r.timestamp)
            from RegistroPonto r where r.user = :user
            order by r.timestamp Desc
            """)
    List<RegistroPontoResponse> findByUserWithPagination(User user, org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: REGISTROS DE PONTO POR PERÍODO COM PAGINAÇÃO
    @Query("""
            Select new com.marcweb.dto.RegistroPontoResponse(r.tipo, r.user.nome, r.user.username, r.ip, r.justificativa, r.timestamp)
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


    @Query(nativeQuery = true, value = "Select * from registros_ponto order by timestamp DESC limit 10")
    List<RegistroPonto> getStatusRecentes();


}
