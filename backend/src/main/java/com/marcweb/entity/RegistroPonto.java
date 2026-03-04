package com.marcweb.entity;

import com.marcweb.dto.ponto.CreateRegistroPonto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "registros_ponto")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Relacionamento com User

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRegistro tipo; // ENTRADA, INICIO_DESCANSO, FIM_DESCANSO, SAIDA


    @Column(nullable = false)
    private LocalDateTime timestamp; // Data e hora do registro

    // Campos de geolocalização (opcionais)
    @Column(name = "latitude", precision = 10)
    private Double latitude;

    @Column(name = "longitude", precision = 11)
    private Double longitude;

    @Column(name = "ip")
    private String ip; // IP do cliente que fez o registro

    @Column(name = "precisao", precision = 10)
    private Double precisao; // Precisão da geolocalização em metros

    @Column(name = "justificativa", length = 500) // Para justificar um registro fora do padrão
    private String justificativa;

    @Column(nullable = false, name = "expediente_id")
    private UUID expedienteId;

    public RegistroPonto(CreateRegistroPonto data, User user, UUID expedienteId, String ip) {
        BeanUtils.copyProperties(data, this);
        this.user = user;
        this.ip = ip;
        this.timestamp = LocalDateTime.now();
        this.expedienteId = expedienteId;
    }

    @Override
    public String toString() {
        return "RegistroPonto{" +
               "id=" + id +
               ", user=" + user +
               ", tipo=" + tipo +
               ", timestamp=" + timestamp +
               ", latitude=" + latitude +
               ", longitude=" + longitude +
               ", ip='" + ip + '\'' +
               ", precisao=" + precisao +
               ", justificativa='" + justificativa + '\'' +
               ", expedienteId=" + expedienteId +
               '}';
    }
}