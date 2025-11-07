package com.example.lmc.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String LOCAL_SERVER_URL = "http://localhost:8080";
    private static final String PRODUCTION_SERVER_URL = "https://lmc.example.com";
    private static final String TERMS_OF_SERVICE_URL = "https://lmc.example.com/termos";
    private static final String REPOSITORY_URL = "https://github.com/example/lmc";

    @Bean
    public OpenAPI lmcOpenAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(List.of(
                        createServer(LOCAL_SERVER_URL, "Servidor local de desenvolvimento"),
                        createServer(PRODUCTION_SERVER_URL, "Servidor de produção")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório do projeto e guias complementares")
                        .url(REPOSITORY_URL));
    }

    private Info buildInfo() {
        return new Info()
                .title("LMC API")
                .description("Documentação completa do Livro de Movimentação de Combustíveis, abrangendo operações de configuração, lançamento diário e geração de relatórios.")
                .termsOfService(TERMS_OF_SERVICE_URL)
                .version("v1")
                .contact(new Contact()
                        .name("Equipe LMC")
                        .email("contato@lmc.example.com")
                        .url(PRODUCTION_SERVER_URL))
                .license(new License()
                        .name("Licença MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private Server createServer(String url, String description) {
        return new Server()
                .url(url)
                .description(description);
    }
}
