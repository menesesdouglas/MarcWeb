package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "tenant_id")
    private String tenantId; // Referência ao tenant (para multi-tenancy)

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nome; // Nome completo do usuário

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ROLE_USER")
    private Role role; // ADMIN, EMPLOYEE

    @Column(nullable = false)
    private Boolean ativo; // true se o usuário está ativo

    @Column(name = "consent_geoloc")
    private Boolean consentGeoloc; // true se o usuário consentiu com geoloc

    @Column(length = 100)
    private String cargo; // Cargo do funcionário

    @Column(nullable = false, length = 14)
    private String cpf; // CPF do funcionário

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt; // Data de criação

    /* Relacionamentos
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RegistroPonto> registrosPonto;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Folga> folgas;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LogAuditoria> logsAuditoria;*/

    // Implementação de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.ativo; // Considera expirado se inativo
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.ativo; // Considera bloqueado se inativo
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Pode implementar lógica de expiração de credenciais
    }

    @Override
    public boolean isEnabled() {
        return this.ativo; // Considera desabilitado se inativo
    }
}