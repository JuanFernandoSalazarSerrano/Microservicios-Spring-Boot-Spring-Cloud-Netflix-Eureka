package com.fsalazar.springcloud.app.gateway.filters;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class SampleGlobalFilter implements GlobalFilter, Ordered{

    private final Logger log = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("Before request to ms");
     
        // Crear un nuevo request con los headers mutados
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("token", "abcdefghi") // Aquí se agrega el header
                .build();
     
        // Crear un nuevo exchange con el request modificado
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();
     
        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            log.info("post response filter");
     
            // Leer el header desde el request modificado
            String token = mutatedExchange.getRequest().getHeaders().getFirst("token");
     
            Optional.ofNullable(token).ifPresent(value -> {
                log.info("Token: " + value);
                mutatedExchange.getResponse().getHeaders().add("token", value); // Agregar a la respuesta
            });
     
            mutatedExchange.getResponse().getCookies()
                    .add("naoya", ResponseCookie.from("naoya", "inoue").build());
            // mutatedExchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        }));
    }

    @Override
    public int getOrder() {
        return 100;
    }
}