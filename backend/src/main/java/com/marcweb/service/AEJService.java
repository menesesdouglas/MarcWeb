package com.marcweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.exceptions.AEJException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AEJService {

    private final AssinaturaDigitalService assinaturaDigitalService;
    private final LogAuditoriaService logAuditoriaService;
    private final ObjectMapper objectMapper;

    /**
     * Gera um Arquivo Eletrônico de Jornada (AEJ) conforme Portaria 671/2021
     */
    public String gerarAEJ(String folhaPontoJson, User user, String chavePrivadaBase64) throws AEJException {
        try {
            // Cria estrutura do AEJ
            Map<String, Object> aej = new HashMap<>();
            aej.put("versao", "1.0");
            aej.put("tipo", "AEJ");
            aej.put("dataGeracao", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            aej.put("empresa", "MarcWeb");
            aej.put("funcionario", user.getNome());
            aej.put("cpf", user.getUsername()); // Assuming username is CPF
            aej.put("dadosJornada", objectMapper.readValue(folhaPontoJson, Map.class));

            // Converte AEJ para JSON
            String aejJson = objectMapper.writeValueAsString(aej);

            // Assina digitalmente o AEJ
            String assinatura = assinaturaDigitalService.assinarDados(aejJson, chavePrivadaBase64);

            // Adiciona assinatura ao AEJ
            Map<String, Object> aejComAssinatura = new HashMap<>(objectMapper.readValue(aejJson, Map.class));
            aejComAssinatura.put("assinaturaDigital", assinatura);

            // Registra log de auditoria
            logAuditoriaService.registrarLog(
                user,
                "Geração de AEJ",
                CategoriaLog.AUDITORIA,
                StatusLog.SUCESSO,
                "AEJ gerado e assinado com sucesso"
            );

            return objectMapper.writeValueAsString(aejComAssinatura);
        } catch (Exception e) {
            try {
                logAuditoriaService.registrarLog(
                    user,
                    "Geração de AEJ",
                    CategoriaLog.AUDITORIA,
                    StatusLog.ERRO,
                    "Falha ao gerar AEJ: " + e.getMessage()
                );
            } catch (Exception logError) {
                e.addSuppressed(logError);
            }
            throw new AEJException("Falha ao gerar AEJ", e);
        }
    }

    /**
     * Valida um AEJ existente
     */
    public boolean validarAEJ(String aejJson, String chavePublicaBase64) throws AEJException {
        try {
            // Extrair assinatura do AEJ
            Map<String, Object> aej = objectMapper.readValue(aejJson, Map.class);
            String assinatura = (String) aej.get("assinaturaDigital");

            // Remover assinatura para validar o conteúdo original
            Map<String, Object> aejSemAssinatura = new HashMap<>(aej);
            aejSemAssinatura.remove("assinaturaDigital");
            String aejSemAssinaturaJson = objectMapper.writeValueAsString(aejSemAssinatura);

            // Verificar assinatura digital
            return assinaturaDigitalService.verificarAssinatura(aejSemAssinaturaJson, assinatura, chavePublicaBase64);
        } catch (Exception e) {
            throw new AEJException("Falha ao validar AEJ", e);
        }
    }
}
