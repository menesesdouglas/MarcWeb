package com.marcweb.hibernate;

import com.marcweb.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Optional;

/// REPONSÁVEL POR INTERCEPTAR A REQUISIÇÃO E COLETAR O HEADER COM O TENANT_ID
@RequiredArgsConstructor
@Component
public class TenantInterceptor implements HandlerInterceptor {


    private final JwtService jwtService;

    /// BUSCA O TENANT_ID NO HEADER E ADICIONA AO CONTEXTO CASO ENCONTRE
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null) {
            token = token.replace("Bearer ", "");
            Claims claims = jwtService.extractAllClaims(token);
            System.out.println(claims.toString());
            String tenantId = claims.get("tenant_id").toString();
            Optional.ofNullable(tenantId).ifPresent(TenantContext::setCurrentTenant);
        } else {
            Optional.ofNullable(request.getHeader("X-Tenant-Id"))
                    .ifPresent(TenantContext::setCurrentTenant);
        }
        return true;
    }

    /// LIMPAR O CURRENT_TENANT DEPOIS DE FINALIZAR A REQUISIÇÃO
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        TenantContext.clear();
    }
}
