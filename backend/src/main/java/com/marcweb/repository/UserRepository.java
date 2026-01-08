package com.marcweb.repository;

import com.marcweb.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // Outros métodos específicos, se necessário
    boolean existsByUsername( String username);

    @Query("Select u.id from User u where u.username = :username")
    Optional<Long> findIdByUsername(@Param("username") String username);
}