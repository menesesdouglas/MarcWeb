package com.marcweb.service;

import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.exceptions.CacheException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class CacheService {

    private final CacheManager cacheManager;
    private final LogAuditoriaService logAuditoriaService;

    /**
     * Obtém dados do cache ou executa a função para obtê-los
     */
    public <T> T getFromCacheOrExecute(String cacheName, String key, CacheDataLoader<T> dataLoader, long timeoutMinutes) throws CacheException {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                throw new CacheException("Cache '" + cacheName + "' não encontrado");
            }

            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null) {
                return (T) valueWrapper.get();
            }

            // Se não estiver no cache, executa o loader
            T data = dataLoader.load();
            cache.put(key, data);

            // Registra log de auditoria
            logAuditoriaService.registrarLog(
                null,
                "Cache miss: " + cacheName + " - " + key,
                CategoriaLog.SISTEMA,
                StatusLog.SUCESSO,
                "Dados carregados e cacheados"
            );

            return data;
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    null,
                    "Erro no cache: " + cacheName + " - " + key,
                    CategoriaLog.SISTEMA,
                    StatusLog.ERRO,
                    "Falha ao acessar cache: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new CacheException("Falha ao acessar cache", e);
        }
    }

    /**
     * Invalida cache específico
     */
    public void invalidateCache(String cacheName, String key) throws CacheException {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }

            logAuditoriaService.registrarLog(
                null,
                "Cache invalidado: " + cacheName + " - " + key,
                CategoriaLog.SISTEMA,
                StatusLog.SUCESSO,
                "Cache invalidado com sucesso"
            );
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    null,
                    "Erro ao invalidar cache: " + cacheName + " - " + key,
                    CategoriaLog.SISTEMA,
                    StatusLog.ERRO,
                    "Falha ao invalidar cache: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new CacheException("Falha ao invalidar cache", e);
        }
    }

    /**
     * Interface funcional para carregamento de dados
     */
    @FunctionalInterface
    public interface CacheDataLoader<T> {
        T load() throws Exception;
    }

    /**
     * Obtém dados de folha de ponto com cache inteligente
     */
    @Cacheable(value = "folhaPontoCache", key = "#userId + '-' + #mes + '-' + #ano", unless = "#result == null")
    public String getFolhaPontoCached(Long userId, int mes, int ano, FolhaPontoService folhaPontoService) throws Exception {
        return folhaPontoService.gerarFolhaPonto(userId, mes, ano);
    }

    /**
     * Atualiza dados de folha de ponto no cache
     */
    @CachePut(value = "folhaPontoCache", key = "#userId + '-' + #mes + '-' + #ano")
    public String updateFolhaPontoCache(Long userId, int mes, int ano, String folhaPontoJson) {
        return folhaPontoJson;
    }

    /**
     * Invalida cache de folha de ponto
     */
    @CacheEvict(value = "folhaPontoCache", key = "#userId + '-' + #mes + '-' + #ano")
    public void invalidateFolhaPontoCache(Long userId, int mes, int ano) {
        // Método vazio - a anotação faz o trabalho
    }

    /**
     * Obtém configurações da empresa com cache
     */
    @Cacheable(value = "configEmpresaCache", key = "'configEmpresa'", unless = "#result == null")
    public Object getConfigEmpresaCached(ConfigEmpresaService configEmpresaService) throws Exception {
        return configEmpresaService.findConfigsEmpresa();
    }

    /**
     * Invalida cache de configurações da empresa
     */
    @CacheEvict(value = "configEmpresaCache", key = "'configEmpresa'")
    public void invalidateConfigEmpresaCache() {
        // Método vazio - a anotação faz o trabalho
    }
}
