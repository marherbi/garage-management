package com.renault.garage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI garageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Garage Management API")
                        .description("API de gestion des garages (exposition des horaires, véhicules, etc.)")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")
                ));
    }
}

