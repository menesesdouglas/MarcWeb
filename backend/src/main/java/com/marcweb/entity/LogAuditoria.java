package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_auditoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp; // Data e hora do log

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Pode ser nulo se for uma ação do sistema
    private User user; // Usuário que realizou a ação

    @Column(nullable = false, length = 200)
    private String acao; // Descrição da ação (ex: login, registro_ponto)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaLog categoria; // PONTO, ADMIN, AUDITORIA, etc.

    @Column(length = 1000) // Detalhes da ação
    private String detalhes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLog status; // SUCESSO, ERRO
}