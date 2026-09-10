package com.tradesettlement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI tradeSettlementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Trade Settlement API")
                .version("v1")
                .description("REST API for real-time trade capture and settlement processing")
                .contact(new Contact().name("Trade Settlement Engineering")));
    }
}
