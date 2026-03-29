package com.andres.springcloud.app.gateway.filters;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
// import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class SampleGlobalFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(SampleGlobalFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        logger.info("ejecutando el filtro antes del request PRE");

        exchange.getRequest().mutate().headers(h -> h.add("token", "abcdefg"));

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("ejecutando filtro POST response");
            String token = exchange.getRequest().getHeaders().getFirst("token");
            if (token != null) {
                logger.info("token: " + token);
                exchange.getResponse().getHeaders().add("token", token);
            }

            Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("token"))
            .ifPresent(value -> {
                logger.info("token2: " + value);
                exchange.getResponse().getHeaders().add("token2", value);
            });;

            exchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "red").build());
            // exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        }));
    }
    
    @Override
    public int getOrder() {
        return 100;
    }

}
