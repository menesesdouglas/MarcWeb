package com.marcweb;

import com.marcweb.entity.Role;
import com.marcweb.entity.User;
import com.marcweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@RequiredArgsConstructor // Gera construtor com campos final (Lombok)
public class MarcWebApplication implements CommandLineRunner { // Implementa CommandLineRunner para execução pós-inicialização

    public static void main(String[] args) {
        SpringApplication.run(MarcWebApplication.class, args);
    }

    private final UserRepository userRepository; // Injeta o repositório de User
    private final PasswordEncoder passwordEncoder; // Injeta o encoder de senha

    @Override
    public void run(String... args) throws Exception {
        // Cria um usuário admin padrão se não existir
        if (userRepository.findByUsername("admin").isEmpty()) {
            var admin = User.builder()
                    .username("admin") // Username padrão
                    .nome("Administrador Padrão") // Nome padrão
                    .password(passwordEncoder.encode("admin123")) // Senha padrão codificada com BCrypt
                    .role(Role.ADMIN) // Papel ADMIN
                    .ativo(true) // Usuário ativo
                    .consentGeoloc(false) // Exemplo de consentimento geoloc
                    .createdAt(LocalDateTime.now()) // Data de criação atual
                    .build();
            userRepository.save(admin); // Salva o usuário no banco
            System.out.println("Usuário admin padrão criado: username='admin', password='admin123' (codificada)");
        }
    }
}