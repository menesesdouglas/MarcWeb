package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "folgas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Folga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Relacionamento com User

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio; // Data de início da folga

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim; // Data de término da folga

    @Column(nullable = false, length = 500)
    private String justificativa; // Justificativa para a folga
}