package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@ToString
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
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

    public LogAuditoria(User user, String acao, CategoriaLog categoria, String detalhes, StatusLog status) {
        this.user = user;
        this.acao = acao;
        this.categoria = categoria;
        this.detalhes = detalhes;
        this.status = status;
    }
}