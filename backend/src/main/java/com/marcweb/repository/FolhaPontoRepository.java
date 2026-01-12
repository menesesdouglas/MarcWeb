package com.marcweb.repository;

import com.marcweb.entity.FolhaPonto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolhaPontoRepository extends JpaRepository<FolhaPonto, Long> {

    FolhaPonto findByUserIdAndMesAndAno(Long userId, int mes, int ano);
}
