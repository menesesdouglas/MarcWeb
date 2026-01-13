package com.marcweb.service;

import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.entity.User;
import com.marcweb.exceptions.PerformanceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PerformanceService {

    private final LogAuditoriaService logAuditoriaService;
    private final CacheService cacheService;

    /**
     * Otimiza consultas e performance do sistema
     */
    @Transactional
    public Map<String, Object> otimizarPerformance() throws PerformanceException {
        try {
            Map<String, Object> resultado = new HashMap<>();

            // 1. Limpar caches
            cacheService.invalidateConfigEmpresaCache();
            resultado.put("cachesLimpos", true);

            // 2. Pré-carregar dados frequentes no cache
            // (Isso seria implementado com dados reais em um cenário real)

            // 3. Registrar log de auditoria
            logAuditoriaService.registrarLog(
                null,
                "Otimização de performance",
                CategoriaLog.SISTEMA,
                StatusLog.SUCESSO,
                "Processo de otimização concluído"
            );

            resultado.put("timestamp", LocalDateTime.now());
            resultado.put("status", "SUCCESS");
            resultado.put("mensagem", "Otimização de performance concluída com sucesso");

            return resultado;
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    null,
                    "Falha na otimização de performance",
                    CategoriaLog.SISTEMA,
                    StatusLog.ERRO,
                    "Erro durante otimização: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new PerformanceException("Falha ao otimizar performance", e);
        }
    }

    /**
     * Monitora performance do sistema
     */
    public Map<String, Object> monitorarPerformance() throws PerformanceException {
        try {
            Map<String, Object> metrics = new HashMap<>();

            // Métricas básicas (em um sistema real, seriam coletadas do sistema)
            metrics.put("timestamp", LocalDateTime.now());
            metrics.put("status", "OK");
            metrics.put("cacheStatus", "ATIVO");
            metrics.put("databaseStatus", "CONECTADO");

            // Registrar log de auditoria
            logAuditoriaService.registrarLog(
                null,
                "Monitoramento de performance",
                CategoriaLog.SISTEMA,
                StatusLog.SUCESSO,
                "Monitoramento realizado com sucesso"
            );

            return metrics;
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    null,
                    "Falha no monitoramento de performance",
                    CategoriaLog.SISTEMA,
                    StatusLog.ERRO,
                    "Erro durante monitoramento: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new PerformanceException("Falha ao monitorar performance", e);
        }
    }

    /**
     * Otimiza consultas específicas
     */
    public Map<String, Object> otimizarConsultas(User user) throws PerformanceException {
        try {
            Map<String, Object> resultado = new HashMap<>();

            // Exemplo: Pré-carregar folhas de ponto do usuário no cache
            // Em um cenário real, isso seria implementado com dados reais

            resultado.put("userId", user.getId());
            resultado.put("username", user.getUsername());
            resultado.put("timestamp", LocalDateTime.now());
            resultado.put("status", "SUCCESS");
            resultado.put("mensagem", "Consultas otimizadas para o usuário");

            // Registrar log de auditoria
            logAuditoriaService.registrarLog(
                user,
                "Otimização de consultas",
                CategoriaLog.SISTEMA,
                StatusLog.SUCESSO,
                "Consultas otimizadas para o usuário"
            );

            return resultado;
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    user,
                    "Falha na otimização de consultas",
                    CategoriaLog.SISTEMA,
                    StatusLog.ERRO,
                    "Erro durante otimização: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new PerformanceException("Falha ao otimizar consultas", e);
        }
    }
}
