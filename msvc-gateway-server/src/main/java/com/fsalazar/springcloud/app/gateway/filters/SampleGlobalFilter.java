package com.fsalazar.springcloud.app.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class SampleGlobalFilter implements GlobalFilter{

    private final Logger logger = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Pre-request filter");

        // returns to the normal flow to send the request to the microservice api
        return chain.filter(exchange).then(
            Mono.fromRunnable(
                () -> {
                    logger.info("post-request filter");

                    exchange.getResponse().getCookies().add("naoya", ResponseCookie.from("naoya", "inoue").build());
                    exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
                }
        )
        );
    }

}
