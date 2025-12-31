package com.marcweb.repository;

import com.marcweb.entity.ConfigEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigEmpresaRepository extends JpaRepository<ConfigEmpresa, Long> {
    // Geralmente, haverá apenas uma configuração principal, mas o ID permite múltiplas se necessário.
    // Pode adicionar métodos específicos se houver critérios de busca únicos.
}