package com.marcweb.service;

import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.entity.User;
import com.marcweb.exceptions.NotificacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class NotificacaoService {

    private final LogAuditoriaService logAuditoriaService;
    private final Map<Long, List<Map<String, Object>>> notificacoesPorUsuario = new HashMap<>();

    /**
     * Envia notificação para um usuário
     */
    public void enviarNotificacao(User user, String titulo, String mensagem, String tipo, String link) throws NotificacaoException {
        try {
            Map<String, Object> notificacao = new HashMap<>();
            notificacao.put("id", System.currentTimeMillis());
            notificacao.put("titulo", titulo);
            notificacao.put("mensagem", mensagem);
            notificacao.put("tipo", tipo);
            notificacao.put("link", link);
            notificacao.put("data", LocalDateTime.now());
            notificacao.put("lida", false);

            // Adiciona notificação ao usuário
            notificacoesPorUsuario.computeIfAbsent(user.getId(), k -> new ArrayList<>()).add(notificacao);

            // Registra log de auditoria
            logAuditoriaService.registrarLog(
                user,
                "Notificação enviada: " + titulo,
                CategoriaLog.SISTEMA,
                StatusLog.SUCESSO,
                "Notificação enviada com sucesso"
            );
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    user,
                    "Erro ao enviar notificação: " + titulo,
                    CategoriaLog.SISTEMA,
                    StatusLog.ERRO,
                    "Falha ao enviar notificação: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new NotificacaoException("Falha ao enviar notificação", e);
        }
    }

    /**
     * Obtém notificações de um usuário
     */
    public List<Map<String, Object>> getNotificacoesPorUsuario(Long userId) {
        return notificacoesPorUsuario.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Marca notificação como lida
     */
    public void marcarComoLida(Long userId, Long notificacaoId) throws NotificacaoException {
        try {
            List<Map<String, Object>> notificacoes = notificacoesPorUsuario.get(userId);
            if (notificacoes != null) {
                notificacoes.stream()
                    .filter(n -> n.get("id").equals(notificacaoId))
                    .findFirst()
                    .ifPresent(n -> n.put("lida", true));
            }

            logAuditoriaService.registrarLog(
                null,
                "Notificação marcada como lida",
                CategoriaLog.SISTEMA,
                StatusLog.SUCESSO,
                "Notificação ID: " + notificacaoId
            );
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    null,
                    "Erro ao marcar notificação como lida",
                    CategoriaLog.SISTEMA,
                    StatusLog.ERRO,
                    "Falha ao marcar notificação: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new NotificacaoException("Falha ao marcar notificação como lida", e);
        }
    }

    /**
     * Envia notificações automáticas com base em eventos
     */
    public void enviarNotificacoesAutomaticas(User user, String evento, Map<String, Object> dados) throws NotificacaoException {
        switch (evento) {
            case "REGISTRO_PONTO":
                enviarNotificacao(
                    user,
                    "Registro de Ponto",
                    "Seu ponto foi registrado com sucesso às " + LocalDateTime.now().toLocalTime(),
                    "INFO",
                    "/meus-pontos"
                );
                break;

            case "FOLHA_PONTO_GERADA":
                enviarNotificacao(
                    user,
                    "Folha de Ponto Gerada",
                    "Sua folha de ponto para o mês " + dados.get("mes") + "/" + dados.get("ano") + " foi gerada",
                    "SUCCESS",
                    "/minhas-folhas"
                );
                break;

            case "HORAS_EXTRAS":
                enviarNotificacao(
                    user,
                    "Horas Extras",
                    "Você tem " + dados.get("horasExtras") + " horas extras no mês " + dados.get("mes") + "/" + dados.get("ano"),
                    "WARNING",
                    "/minhas-folhas"
                );
                break;

            case "FALTA_REGISTRO":
                enviarNotificacao(
                    user,
                    "Aviso de Falta",
                    "Você não registrou ponto hoje. Lembre-se de registrar seu ponto diariamente.",
                    "DANGER",
                    "/registrar-ponto"
                );
                break;

            default:
                throw new NotificacaoException("Evento de notificação desconhecido: " + evento);
        }
    }
}
