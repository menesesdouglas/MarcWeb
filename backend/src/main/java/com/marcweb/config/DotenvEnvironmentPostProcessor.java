package com.marcweb.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentPostProcessor que carrega variáveis do arquivo .env antes do contexto Spring ser inicializado.
 * Assim garantimos que placeholders em application-*.yml (ex.: ${DB_URL_TEST}) sejam resolvidos durante a
 * avaliação das condições de auto-configuração (usado também em testes).
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // Tenta carregar .env a partir de múltiplos locais relativos ao processo de execução.
            // Isso corrige um problema onde os testes (executados dentro de 'backend') não
            // encontravam 'backend/.env' (pois o working dir já era 'backend').
            Dotenv dotenv = null;
            String[] candidates = new String[] { "backend", ".", ".." };
            for (String dir : candidates) {
                try {
                    Dotenv d = Dotenv.configure()
                            .directory(dir)
                            .ignoreIfMissing()
                            .load();
                    // Se encontrou variáveis nesta instância, usa-a e interrompe
                    if (d != null && d.entries() != null && d.entries().iterator().hasNext()) {
                        dotenv = d;
                        break;
                    }
                } catch (Exception ignored) {
                    // ignora e tenta próximo candidato
                }
            }
            // fallback: tenta carregar sem especificar diretório (Dotenv tentará achar .env no working dir)
            if (dotenv == null) {
                try {
                    dotenv = Dotenv.configure()
                            .ignoreIfMissing()
                            .load();
                } catch (Exception ignored) {
                }
            }

            Map<String, Object> map = new HashMap<>();
            dotenv.entries().forEach(entry -> map.put(entry.getKey(), entry.getValue()));

            if (!map.isEmpty()) {
                // Insere as propriedades carregadas na primeira posição para que sejam usadas na resolução de placeholders
                environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", map));
            }
        } catch (Exception ignored) {
            // Não falhar a inicialização caso não exista .env — o ambiente pode fornecer as variáveis por outro meio.
        }
    }
}