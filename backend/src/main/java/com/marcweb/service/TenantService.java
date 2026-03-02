package com.marcweb.service;

import com.marcweb.dto.tenant.CreateTenantRequest;
import com.marcweb.dto.tenant.TenantResponse;
import com.marcweb.entity.Tenant;
import com.marcweb.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TenantService {

    private final TenantRepository repository;
    private final TenantSchemaService schemaService;

    public List<TenantResponse> findAll() {
        return repository.findAllAtivas();
    }

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        // Verificar se o tenantId já existe
        if (repository.existsByTenantId(request.getTenantId())) {
            throw new IllegalArgumentException("Já existe um tenant com este identificador");
        }

        // Verificar se o CNPJ já existe
        if (repository.findByCnpj(request.getCnpj()).isPresent()) {
            throw new IllegalArgumentException("Já existe um tenant com este CNPJ");
        }

        // Criar o tenant
        // Define o plano (default: FREEMIUM se não especificado)
        String plano = request.getPlano();
        if (plano == null || plano.isBlank()) {
            plano = "FREEMIUM";
        }
        
        Tenant tenant = Tenant.builder()
                .tenantId(request.getTenantId())
                .nome(request.getNome())
                .cnpj(request.getCnpj())
                .dominio(request.getDominio())
                .plano(plano)
                .ativo(true)
                .build();

        tenant = repository.save(tenant);
        log.info("Tenant {} criado com sucesso", tenant.getTenantId());

        // Criar o schema do tenant
        try {
            schemaService.createTenantSchema(tenant.getTenantId());
            log.info("Schema para tenant {} criado com sucesso", tenant.getTenantId());
        } catch (Exception e) {
            log.error("Erro ao criar schema para tenant {}: {}", tenant.getTenantId(), e.getMessage());
            // Se falhar a criação do schema, ainda assim retornamos o tenant criado
            // O schema pode ser criado sob demanda na primeira requisição
        }

        return new TenantResponse(tenant.getNome(), tenant.getTenantId(), tenant.getPlano());
    }
}
