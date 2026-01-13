package com.marcweb.controller;

import com.marcweb.entity.User;
import com.marcweb.exceptions.NotificacaoException;
import com.marcweb.service.NotificacaoService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<List<Map<String, Object>>> getMinhasNotificacoes() {
        try {
            UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
            User user = userService.findByUsername(userDetails.getUsername());
            List<Map<String, Object>> notificacoes = notificacaoService.getNotificacoesPorUsuario(user.getId());
            return ResponseEntity.ok(notificacoes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/marcar-lida")
    public ResponseEntity<String> marcarComoLida(@RequestParam Long notificacaoId) {
        try {
            UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
            User user = userService.findByUsername(userDetails.getUsername());
            notificacaoService.marcarComoLida(user.getId(), notificacaoId);
            return ResponseEntity.ok("Notificação marcada como lida");
        } catch (NotificacaoException e) {
            return ResponseEntity.internalServerError().body("Falha ao marcar notificação: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/enviar")
    public ResponseEntity<String> enviarNotificacao(
            @RequestParam Long userId,
            @RequestParam String titulo,
            @RequestParam String mensagem,
            @RequestParam String tipo,
            @RequestParam String link) {
        try {
            User user = userService.findById(userId);
            notificacaoService.enviarNotificacao(user, titulo, mensagem, tipo, link);
            return ResponseEntity.ok("Notificação enviada com sucesso");
        } catch (NotificacaoException e) {
            return ResponseEntity.internalServerError().body("Falha ao enviar notificação: " + e.getMessage());
        }
    }
}
