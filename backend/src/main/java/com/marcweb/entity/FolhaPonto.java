package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "folhas_ponto")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolhaPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer mes; // 1-12

    @Column(nullable = false)
    private Integer ano; // ex: 2026

    @Column(columnDefinition = "TEXT")
    private String dados; // JSON estruturado conforme SDD

    @Column(name = "horas_trabalhadas", precision = 10, scale = 2)
    private BigDecimal horasTrabalhadas;

    @Column(name = "horas_extras", precision = 10, scale = 2)
    private BigDecimal horasExtras;

    @Column(name = "horas_debito", precision = 10, scale = 2)
    private BigDecimal horasDebito;

    @Column(name = "dias_trabalhados")
    private Integer diasTrabalhados;

    @Column(name = "dias_falta")
    private Integer diasFalta;

    @CreatedDate
    @Column(name = "gerada_em")
    private LocalDateTime geradaEm;

    // Método helper para calcular saldo
    public BigDecimal getSaldoHoras() {
        if (horasExtras == null) horasExtras = BigDecimal.ZERO;
        if (horasDebito == null) horasDebito = BigDecimal.ZERO;
        return horasExtras.subtract(horasDebito);
    }
}