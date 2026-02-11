package com.marcweb;

import com.marcweb.entity.ConfigEmpresa; // Import necessário
import com.marcweb.entity.Role;
import com.marcweb.entity.User;
import com.marcweb.repository.ConfigEmpresaRepository; // Import necessário
import com.marcweb.repository.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List; // Import necessário


@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@RequiredArgsConstructor // Gera construtor com campos final (Lombok)
public class MarcWebApplication implements CommandLineRunner { // Implementa CommandLineRunner para execução pós-inicialização

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(MarcWebApplication.class, args);
    }

    private final UserRepository userRepository; // Injeta o repositório de User
    private final ConfigEmpresaRepository configEmpresaRepository; // Injeta o repositório de ConfigEmpresa
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
                    .cpf("000.000.000-00") // CPF placeholder para admin padrão
                    .createdAt(LocalDateTime.now()) // Data de criação atual
                    .build();

            userRepository.save(admin); // Salva o usuário no banco
            System.out.println("Usuário admin padrão criado: username='admin', password='admin123' (codificada)");
        }

        /*  Cria uma configuração padrão da empresa se não existir nenhuma
         Usamos findAll().isEmpty() pois pode haver mais de uma configuração no futuro
         ou critérios específicos para a "configuração ativa". Para MVP, apenas uma é esperada. */
        List<ConfigEmpresa> configsExistentes = configEmpresaRepository.findAll(); // Busca todas as configurações
        if (configsExistentes.isEmpty()) { // Se a lista estiver vazia, cria a padrão
            var configPadrao = ConfigEmpresa.builder()
                    .nomeEmpresa("Empresa Exemplo LTDA") // Nome padrão
                    .latCentro(-12.2658) // Coordenada padrão (Feira de Santana)
                    .longCentro(-38.9668) // Coordenada padrão (Feira de Santana)
                    .raioGeoloc(100) // Raio padrão em metros
                    .geolocAtiva(false) // Geolocalização desativada por padrão
                    .build();
            configEmpresaRepository.save(configPadrao); // Salva a configuração no banco
            System.out.println("Configuração da empresa padrão criada.");
        } else {
            // Opcional: Logar que já existia configuração
            System.out.println("Configuração da empresa já existente, não foi criada uma nova padrão.");
        }
    }
}
