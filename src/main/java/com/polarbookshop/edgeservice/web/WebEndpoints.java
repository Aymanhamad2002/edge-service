package com.polarbookshop.edgeservice.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebEndpoints {

    private static final Logger log = LoggerFactory.getLogger(WebEndpoints.class);

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {

        return RouterFunctions.route()
                .GET("/catalog-fallback", request -> {
                    log.warn("Catalog request failed; returning the GET fallback response");
                    return ServerResponse.ok()
                            .bodyValue("Catalog service is currently unavailable.");
                })
                .POST("/catalog-fallback", request -> {
                    log.warn("Catalog request failed; returning service unavailable for POST");
                    return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                })
                .build();
    }
}