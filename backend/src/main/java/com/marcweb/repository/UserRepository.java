package com.marcweb.repository;

import com.marcweb.dto.UserDTO;
import com.marcweb.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // Outros métodos específicos, se necessário
    boolean existsByUsername( String username);

    @Query("Select u.id from User u where u.username = :username")
    Optional<Long> findIdByUsername(@Param("username") String username);

    // CONSULTA OTIMIZADA: USUÁRIOS COM PAGINAÇÃO
    @Query("SELECT u FROM User u ORDER BY u.nome")
    List<User> findAllWithPagination(org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: USUÁRIOS ATIVOS
    @Query("SELECT u FROM User u WHERE u.ativo = true ORDER BY u.nome")
    List<User> findAllAtivos();

    // CONSULTA OTIMIZADA: USUÁRIOS ATIVOS COM PAGINAÇÃO
    @Query("SELECT u FROM User u WHERE u.ativo = true ORDER BY u.nome")
    List<User> findAllAtivosWithPagination(org.springframework.data.domain.Pageable pageable);

    // CONSULTA OTIMIZADA: CONTAGEM DE USUÁRIOS ATIVOS
    @Query("SELECT COUNT(u) FROM User u WHERE u.ativo = true")
    long countAtivos();

    // CONSULTA OTIMIZADA: BUSCA POR NOME (LIKE)
    @Query("SELECT u FROM User u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY u.nome")
    List<User> findByNomeContaining(@Param("nome") String nome);

    // CONSULTA OTIMIZADA: BUSCA POR NOME COM PAGINAÇÃO
    @Query("SELECT u FROM User u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY u.nome")
    List<User> findByNomeContainingWithPagination(@Param("nome") String nome, Pageable pageable);
}
