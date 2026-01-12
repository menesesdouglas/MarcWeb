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


}