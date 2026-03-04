package com.marcweb.security;

import com.marcweb.hibernate.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration // Marca como uma classe de configuração do Spring
@EnableWebSecurity // Habilita a segurança web do Spring
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true) // HABILITANDO METÓDOS DE SEGURANÇA NOS END POINTS
@RequiredArgsConstructor // Gera construtor com campos final (Lombok) - NÃO USAREMOS PARA jwtAuthenticationFilter
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, TenantInterceptor tenantInterceptor) throws Exception {
        http.cors().and()
                .csrf(csrf -> csrf.disable()) // Desativa CSRF para API stateless (sem sessões)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Política de sessão stateless
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/teste/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tenant").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tenantInterceptor, UsernamePasswordAuthenticationFilter.class)  // Adiciona o filtro identificador do tenant antes do filtro padrão de login
                .addFilterAfter(jwtAuthenticationFilter, TenantInterceptor.class); // Adiciona o filtro jwt do tenant depois do filtro identificador de tenant

        return http.build(); // Constroi e retorna o objeto SecurityFilterChain
    }

    // Configuração CORS - Permitindo origens, métodos e headers específicos
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite todas as origens (ajuste para produção!)
        configuration.setAllowedOriginPatterns(List.of("*"));
        // Permite métodos HTTP comuns
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permite todos os headers
        configuration.setAllowedHeaders(List.of("*"));
        // Permite credenciais (como cookies, headers Authorization)
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica as configurações para todos os caminhos /**
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean // Encoder de senha BCrypt (conforme SDD)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean // Gerenciador de autenticação (necessário para o controller de login)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}