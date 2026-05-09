package com.example.moviebot.config;



import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usersApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Users REST API")
                        .description("REST API for managing users stored in SQLite")
                        .version("1.0"));
    }
}
