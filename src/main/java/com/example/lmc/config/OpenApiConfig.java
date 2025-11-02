package com.example.lmc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                        .description("API de controle do Livro de Movimentação de Combustíveis.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Equipe LMC")
                                .email("contato@example.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor local")
                ));
    }
}
