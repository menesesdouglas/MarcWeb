package com.marcweb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serviço responsável por operações relacionadas ao JWT (JSON Web Token).
 * Fornece funcionalidades para geração, validação e extração de informações de tokens JWT.
 * <p>
 * Este serviço utiliza uma chave secreta configurada via variáveis de ambiente
 * para assinar e verificar tokens, garantindo segurança na autenticação.
 *
 * @author MarcWeb Team
 * @version 2.0
 * @since 1.0
 */
@Service
public class JwtService {

    @Autowired
    private Environment env;

    private String getSecretKey() {
        // Tenta ler a propriedade configurada no YAML (application.security.jwt.secret-key)
        String secretKey = env.getProperty("application.security.jwt.secret-key");

        // Se não encontrar, tenta ler diretamente a variável de ambiente JWT_SECRET
        if (secretKey == null || secretKey.isBlank()) {
            secretKey = env.getProperty("JWT_SECRET");
        }

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET não configurado! Configure a propriedade application.security.jwt.secret-key ou a variável de ambiente JWT_SECRET");
        }

        // Valida se a chave é Base64 válida
        try {
            // Testa se a string pode ser decodificada como Base64
            Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT_SECRET inválido: a chave deve ser uma string Base64 válida. Erro: " + e.getMessage() +
                                               "\nExemplo de geração de chave válida: openssl rand -base64 48");
        }

        return secretKey;
    }

    /**
     * Identificador da chave atual (kid). Deve ser curto e único (ex: key-2026-01).
     */
    @Value("${application.security.jwt.kid:default}")
    private String keyId;

    /**
     * Chaves anteriores para permitir validação de tokens antigos no processo de rotação.
     * Formato esperado: id1:base64key1,id2:base64key2
     */
    @Value("${application.security.jwt.previous-keys:}")
    private String previousKeys;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-threshold:86400000}")
    private long refreshThreshold; // 24 horas antes da expiração

    // Mapa de kid -> chave base64 (inclui a chave atual)
    private final Map<String, String> keyMap = new HashMap<>();

    /**
     * Verifica se o token JWT precisa ser renovado baseado no tempo de expiração.
     * Retorna true se faltar menos de 24 horas para a expiração.
     *
     * @param token Token JWT a ser verificado
     * @return true se o token precisar ser renovado, false caso contrário
     * @throws Exception se ocorrer erro na validação do token
     */
    public boolean needsRefresh(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            long timeUntilExpiration = expiration.getTime() - now.getTime();
            return timeUntilExpiration <= refreshThreshold;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai o nome de usuário do token JWT.
     *
     * @param token Token JWT do qual extrair o username
     * @return Nome de usuário contido no token
     * @throws Exception se o token for inválido
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai uma informação específica (claim) do token JWT usando uma função resolver.
     *
     * @param token          Token JWT do qual extrair a informação
     * @param claimsResolver Função que define qual claim extrair
     * @return Valor do claim extraído
     * @throws Exception se o token for inválido
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrai todas as informações (claims) do token JWT.
     *
     * @param token Token JWT a ser analisado
     * @return Objeto Claims contendo todas as informações do token
     * @throws Exception se o token for inválido ou a assinatura não puder ser verificada
     */
    /**
     * Extrai todas as informações (claims) do token JWT usando um SigningKeyResolver
     * que resolve a chave com base no header 'kid'.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKeyResolver(getSigningKeyResolver()) // Resolve a chave dinamicamente baseado no kid
                .build()
                .parseClaimsJws(token) // Faz o parse do token JWS
                .getBody(); // Retorna o corpo (claims) do token

    }

    /**
     * Gera a chave de assinatura usada para verificar tokens JWT a partir da chave atual.
     *
     * @return Chave HMAC adequada para assinatura HS256
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Inicializa o mapa de chaves (keyId -> base64Key) a partir das propriedades configuradas.
     * Coloca também a chave atual (secretKey) com o kid configurado.
     */
    @PostConstruct
    private void initKeys() {
        // adiciona chave atual
        String secretKey = getSecretKey();
        if (keyId != null && !keyId.isBlank() && secretKey != null && !secretKey.isBlank()) {
            keyMap.put(keyId, secretKey);
        }

        if (previousKeys != null && !previousKeys.isBlank()) {
            // formato: id1:base64key1,id2:base64key2
            String[] entries = previousKeys.split(",");
            for (String e : entries) {
                String trimmed = e.trim();
                if (trimmed.isEmpty()) continue;
                String[] parts = trimmed.split(":", 2);
                if (parts.length == 2) {
                    String id = parts[0].trim();
                    String base64 = parts[1].trim();
                    if (!id.isEmpty() && !base64.isEmpty()) {
                        keyMap.putIfAbsent(id, base64);
                    }
                }
            }
        }
    }

    /**
     * Retorna um SigningKeyResolver que utiliza o header 'kid' para selecionar a chave correta.
     */
    private SigningKeyResolver getSigningKeyResolver() {
        return new SigningKeyResolver() {
            @Override
            public Key resolveSigningKey(JwsHeader header, Claims claims) {
                String kid = header != null ? header.getKeyId() : null;
                String base64Key = getKeyByKid(kid);
                byte[] keyBytes = Decoders.BASE64.decode(base64Key);
                return Keys.hmacShaKeyFor(keyBytes);
            }

            @Override
            public Key resolveSigningKey(JwsHeader header, byte[] payload) {
                // fallback para a mesma lógica (algumas versões da API fornecem o payload como byte[])
                String kid = header != null ? header.getKeyId() : null;
                String base64Key = getKeyByKid(kid);
                byte[] keyBytes = Decoders.BASE64.decode(base64Key);
                return Keys.hmacShaKeyFor(keyBytes);
            }
        };
    }

    private String getKeyByKid(String kid) {
        if (kid == null || kid.isBlank()) {
            // fallback para chave atual
            return getSecretKey();
        }
        String key = keyMap.get(kid);
        if (key == null) {
            throw new IllegalArgumentException("Unknown key id: " + kid);
        }
        return key;
    }

    /**
     * Gera um token JWT para o usuário fornecido sem claims extras.
     *
     * @param userDetails Informações do usuário autenticado
     * @return Token JWT gerado
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Gera um token JWT para o usuário fornecido com claims extras.
     *
     * @param extraClaims Claims adicionais a serem incluídos no token
     * @param userDetails Informações do usuário autenticado
     * @return Token JWT gerado
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Método interno para construir o token JWT com todos os parâmetros necessários.
     *
     * @param extraClaims Claims adicionais
     * @param userDetails Informações do usuário
     * @param expiration  Tempo de expiração em milissegundos
     * @return Token JWT completo e assinado
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setHeaderParam("kid", keyId) // Define o identificador da chave atual
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Assina o token com a chave atual
                .compact(); // Compacta para string
    }

    /**
     * Verifica se o token JWT é válido para o usuário fornecido.
     * Verifica se o username corresponde e se o token não expirou.
     *
     * @param token       Token JWT a ser validado
     * @param userDetails Informações do usuário esperado
     * @return true se o token for válido, false caso contrário
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica se o token JWT expirou.
     *
     * @param token Token JWT a ser verificado
     * @return true se o token expirou, false se ainda é válido
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrai a data de expiração do token JWT.
     *
     * @param token Token JWT do qual extrair a expiração
     * @return Data de expiração do token
     * @throws Exception se o token for inválido
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


}