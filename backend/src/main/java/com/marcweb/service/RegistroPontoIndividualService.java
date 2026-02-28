package com.marcweb.service;

import com.marcweb.dto.ponto.RegistroPontoIndividualDTO;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.Tenant;
import com.marcweb.entity.User;
import com.marcweb.exceptions.RegistroPontoNotFoundException;
import com.marcweb.repository.RegistroPontoRepository;
import com.marcweb.repository.TenantRepository;
import com.marcweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistroPontoIndividualService {

    private final RegistroPontoRepository registroPontoRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public RegistroPontoIndividualDTO buscarRegistroPontoIndividual(Long registroId, Long userId) {
        // Busca o registro de ponto com usuário carregado (resolve LazyInitializationException)
        RegistroPonto registro = registroPontoRepository.findByIdWithUser(registroId)
                .orElseThrow(() -> new RegistroPontoNotFoundException("Registro de ponto não encontrado"));

        // Verifica se o usuário tem permissão para acessar este registro
        if (!registro.getUser().getId().equals(userId)) {
            throw new SecurityException("Acesso negado: você só pode visualizar seus próprios registros de ponto");
        }

        // Busca o tenant - usar schema público para consulta de tenants
        Tenant tenant = tenantRepository.findByTenantId(registro.getUser().getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        // Busca o usuário
        User user = registro.getUser();

        // Cria o DTO com todas as informações
        return RegistroPontoIndividualDTO.builder()
                .id(registro.getId())
                .nomeUsuario(user.getNome())
                .cpf(user.getCpf())
                .cargo(user.getCargo() != null ? user.getCargo() : "Não informado")
                .tenantNome(tenant.getNome())
                .tenantCnpj(tenant.getCnpj())
                .tipo(registro.getTipo())
                .timestamp(registro.getTimestamp())
                .latitude(registro.getLatitude())
                .longitude(registro.getLongitude())
                .precisao(registro.getPrecisao())
                .ip(registro.getIp())
                .justificativa(registro.getJustificativa())
                .expedienteId(registro.getExpedienteId().toString())
                .dataEmissao(LocalDateTime.now())
                .build();
    }
}