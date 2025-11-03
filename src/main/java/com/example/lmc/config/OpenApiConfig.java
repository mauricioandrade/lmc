package com.example.lmc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lmcOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LMC API")
                        .description("Documentação completa do Livro de Movimentação de Combustíveis, abrangendo operações de configuração, lançamento diário e geração de relatórios.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Equipe LMC")
                                .email("contato@example.com")
                                .url("https://example.com/lmc"))
                        .license(new License()
                                .name("Licença MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor local de desenvolvimento"),
                        new Server()
                                .url("https://lmc.example.com")
                                .description("Servidor de produção")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório do projeto e guias complementares")
                        .url("https://github.com/example/lmc"));
    }
}
