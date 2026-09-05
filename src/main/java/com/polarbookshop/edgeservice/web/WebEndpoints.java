package com.polarbookshop.edgeservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebEndpoints {

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {

        return RouterFunctions.route()
                .GET("/catalog-fallback", request ->
                        ServerResponse.ok()
                                .bodyValue("Catalog service is currently unavailable.")
                )
                .POST("/catalog-fallback", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .build()
                )
                .build();
    }
}