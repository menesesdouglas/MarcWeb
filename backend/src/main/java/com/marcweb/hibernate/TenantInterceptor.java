package com.marcweb.hibernate;

import com.marcweb.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;

/// REPONSÁVEL POR INTERCEPTAR A REQUISIÇÃO E COLETAR O HEADER COM O TENANT_ID
@RequiredArgsConstructor
@Component
public class TenantInterceptor extends OncePerRequestFilter {


    private final JwtService jwtService;

    /// BUSCA O TENANT_ID NO HEADER E ADICIONA AO CONTEXTO CASO ENCONTRE
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        String tenantId = null;
        
        if (token != null && !token.isBlank()) {
            try {
                token = token.replace("Bearer ", "");
                Claims claims = jwtService.extractAllClaims(token);
                Object tenantIdClaim = claims.get("tenant_id");
                if (tenantIdClaim != null) {
                    tenantId = tenantIdClaim.toString();
                }
            } catch (Exception e) {
                // Log do erro mas continua para permitir que o filtro seguinte tente obter o tenant
                // Se o token for inválido, o Spring Security irá tratar isso
            }
        }
        
        // Tentar obter do header X-Tenant-Id se não obteve do token
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = request.getHeader("X-Tenant-Id");
        }
        
        // Define o tenant no contexto
        if (tenantId != null && !tenantId.isBlank()) {
            TenantContext.setCurrentTenant(tenantId);
        }
        try{
            filterChain.doFilter(request, response);
        }finally {
            TenantContext.clear();
        }
    }
}
