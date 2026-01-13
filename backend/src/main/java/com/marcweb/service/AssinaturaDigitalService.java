package com.marcweb.service;

import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.exceptions.AssinaturaDigitalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class AssinaturaDigitalService {

    private final LogAuditoriaService logAuditoriaService;

    /**
     * Gera um par de chaves RSA (pública e privada)
     * @return Array onde [0] = chave privada, [1] = chave pública
     */
    public String[] gerarParDeChaves() throws AssinaturaDigitalException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            // Codifica as chaves em Base64 para armazenamento
            String chavePrivada = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
            String chavePublica = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

            return new String[]{chavePrivada, chavePublica};
        } catch (NoSuchAlgorithmException e) {
            throw new AssinaturaDigitalException("Algoritmo RSA não disponível", e);
        }
    }

    /**
     * Assina dados digitalmente usando a chave privada
     * @param dados Dados a serem assinados
     * @param chavePrivadaBase64 Chave privada em formato Base64
     * @return Assinatura digital em Base64
     */
    public String assinarDados(String dados, String chavePrivadaBase64) throws AssinaturaDigitalException {
        try {
            // Decodifica a chave privada
            byte[] chavePrivadaBytes = Base64.getDecoder().decode(chavePrivadaBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(chavePrivadaBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Cria a assinatura
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(dados.getBytes());

            // Retorna a assinatura em Base64
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new AssinaturaDigitalException("Falha ao assinar dados", e);
        }
    }

    /**
     * Verifica a assinatura digital
     * @param dados Dados originais
     * @param assinatura Assinatura digital em Base64
     * @param chavePublicaBase64 Chave pública em formato Base64
     * @return true se a assinatura for válida, false caso contrário
     */
    public boolean verificarAssinatura(String dados, String assinatura, String chavePublicaBase64) throws AssinaturaDigitalException {
        try {
            // Decodifica a chave pública
            byte[] chavePublicaBytes = Base64.getDecoder().decode(chavePublicaBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(chavePublicaBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Verifica a assinatura
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(dados.getBytes());

            byte[] assinaturaBytes = Base64.getDecoder().decode(assinatura);
            return signature.verify(assinaturaBytes);
        } catch (Exception e) {
            throw new AssinaturaDigitalException("Falha ao verificar assinatura", e);
        }
    }

    /**
     * Assina dados e registra log de auditoria
     */
    public String assinarDadosComAuditoria(String dados, String chavePrivadaBase64, User user, String acao) throws AssinaturaDigitalException {
        try {
            String assinatura = assinarDados(dados, chavePrivadaBase64);

            // Registra log de auditoria
            logAuditoriaService.registrarLog(
                user,
                "Assinatura digital: " + acao,
                CategoriaLog.AUDITORIA,
                StatusLog.SUCESSO,
                "Dados assinados com sucesso"
            );

            return assinatura;
        } catch (Exception e) {
            try {
                // Registra log de erro
                logAuditoriaService.registrarLog(
                    user,
                    "Assinatura digital: " + acao,
                    CategoriaLog.AUDITORIA,
                    StatusLog.ERRO,
                    "Falha ao assinar dados: " + e.getMessage()
                );
            } catch (Exception logError) {
                // Se falhar ao registrar log, pelo menos não perde a exceção original
                e.addSuppressed(logError);
            }
            throw new AssinaturaDigitalException("Falha ao assinar dados com auditoria", e);
        }
    }
}
