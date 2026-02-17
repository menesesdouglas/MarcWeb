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
        if (token != null) {
            token = token.replace("Bearer ", "");
            Claims claims = jwtService.extractAllClaims(token);
            String tenantId = claims.get("tenant_id").toString();
            Optional.ofNullable(tenantId).ifPresent(TenantContext::setCurrentTenant);
        } else {
            Optional.ofNullable(request.getHeader("X-Tenant-Id"))
                    .ifPresent(TenantContext::setCurrentTenant);
        }

        try{
            filterChain.doFilter(request, response);
        }finally {
            TenantContext.clear();
        }
    }
}
