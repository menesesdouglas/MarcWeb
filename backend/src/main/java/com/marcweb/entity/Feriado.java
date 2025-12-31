package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "feriados")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feriado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data; // Data do feriado

    @Column(nullable = false)
    private String nome; // Nome do feriado

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemFeriado origem; // API (BrasilAPI), MANUAL
}