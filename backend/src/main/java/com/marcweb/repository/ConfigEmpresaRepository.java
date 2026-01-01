package com.marcweb.repository;

import com.marcweb.entity.ConfigEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigEmpresaRepository extends JpaRepository<ConfigEmpresa, Long> {
    // Geralmente, haverá apenas uma configuração principal, mas o ID permite múltiplas se necessário.
    // Pode adicionar métodos específicos se houver critérios de busca únicos.

    @Query(nativeQuery = true, value = "Select * from configs_empresa order by id limit 1")
    Optional<ConfigEmpresa> findFirst();
}