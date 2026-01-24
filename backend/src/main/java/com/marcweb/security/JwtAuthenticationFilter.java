package com.marcweb.security;

import com.marcweb.entity.User;
import com.marcweb.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        log.info(authHeader);
        log.debug("Filtro JWT chamado para: {}", request.getRequestURI());

        // Verifica se o header Authorization está presente e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Header Authorization ausente ou não começa com 'Bearer '. Continuando cadeia.");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // Remove o prefixo "Bearer "
        log.debug("Token JWT extraído: {}", jwt.substring(0, Math.min(20, jwt.length())) + "..."); // Log parcial do token

        try {
            username = jwtService.extractUsername(jwt); // Extrai o username do token JWT
            log.debug("Username extraído do token: {}", username);
        } catch (Exception e) {
            log.error("Erro ao extrair username do token JWT", e);
            filterChain.doFilter(request, response);
            return;
        }


        // Se o username foi extraído e ainda não há autenticação no contexto do Spring
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("Username não nulo e contexto de segurança vazio. Carregando UserDetails...");
            // Carrega os detalhes do usuário a partir do username
            User user  = userService.findByUsername(username);
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .roles(user.getRole().name())
                    .build();

            // Verifica se o token JWT é válido para o userDetails carregado
            if (jwtService.isTokenValid(jwt, userDetails)) {
                log.debug("Token JWT é válido para o usuário: {}. Configurando autenticação.", username);
                // Cria um token de autenticação
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // O objeto UserDetails
                        null, // Credenciais (não são necessárias aqui)
                        userDetails.getAuthorities() // As autoridades (roles) do usuário
                );
                // Adiciona detalhes da requisição à autenticação
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Configura a autenticação no contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("Token JWT inválido para o usuário: {}", username);
            }
        } else {
            log.debug("Username nulo ou contexto de segurança já contém autenticação.");
        }

        // Continua a cadeia de filtros
        log.debug("Continuando cadeia de filtros.");
        filterChain.doFilter(request, response);
    }



}