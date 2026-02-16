package com.marcweb.repository;

import com.marcweb.dto.tenant.TenantResponse;
import com.marcweb.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    @Query("Select new com.marcweb.dto.tenant.TenantResponse(t.nome, t.tenantId) from Tenant t where t.ativo = true")
    List<TenantResponse> findAllAtivas();
}
