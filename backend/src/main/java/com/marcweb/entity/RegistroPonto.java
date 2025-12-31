package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
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
}