package com.marcweb.repository;

import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.entity.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigEmpresaRepository extends JpaRepository<ConfigEmpresa, Long> {
    // Geralmente, haverá apenas uma configuração principal, mas o ID permite múltiplas se necessário.
    // Pode adicionar métodos específicos se houver critérios de busca únicos.

    // Busca no schema público (ignora contexto de tenant)
    @Query(nativeQuery = true, value = "SELECT * FROM public.configs_empresa ORDER BY id LIMIT 1")
    Optional<ConfigEmpresa> findFirstInPublicSchema();

    @Query(nativeQuery = true, value = "Select * from configs_empresa order by id limit 1")
    Optional<ConfigEmpresa> findFirst();

    // Busca todas no schema público
    @Query(nativeQuery = true, value = "SELECT * FROM public.configs_empresa")
    List<ConfigEmpresa> findAllInPublicSchema();

    @Query(nativeQuery = true, value = "Select c.plano from configs_empresa c order by id limit 1")
    Plano findPlano();
}
