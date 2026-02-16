package com.marcweb.service;

import com.marcweb.dto.tenant.TenantResponse;
import com.marcweb.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TenantService {

    private final TenantRepository repository;

    public List<TenantResponse> findAll() {
        return repository.findAllAtivas();
    }
}
