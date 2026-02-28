package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tenants")
@Entity
public class Tenant {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true, name = "tenant_id")
    private String tenantId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cnpj;

    private String dominio;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
