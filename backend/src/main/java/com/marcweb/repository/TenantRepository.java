package com.marcweb.repository;

import com.marcweb.dto.tenant.TenantResponse;
import com.marcweb.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.QueryHints;
import org.hibernate.jpa.AvailableHints;

import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    @Query("Select new com.marcweb.dto.tenant.TenantResponse(t.nome, t.tenantId) from Tenant t where t.ativo = true")
    List<TenantResponse> findAllAtivas();

    @Query(value = "SELECT * FROM public.tenants t WHERE t.tenant_id = :tenantId", nativeQuery = true)
    Optional<Tenant> findByTenantId(String tenantId);

    boolean existsByTenantId(String tenantId);

    @Query(value = "SELECT * FROM public.tenants t WHERE t.cnpj = :cnpj", nativeQuery = true)
    Optional<Tenant> findByCnpj(String cnpj);
}
