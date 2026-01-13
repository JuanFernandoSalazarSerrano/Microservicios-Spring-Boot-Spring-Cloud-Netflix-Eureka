package com.fsalazar.springcloud.app.gateway.filters.factory;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SampleCookieGatewayFilterFactory extends AbstractGatewayFilterFactory<SampleCookieGatewayFilterFactory.ConfigurationCookie>{
    
    private final Logger log = LoggerFactory.getLogger(SampleCookieGatewayFilterFactory.class);

    SampleCookieGatewayFilterFactory(){
        super(ConfigurationCookie.class);
    }

    @Override
    public GatewayFilter apply(ConfigurationCookie config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            
            log.info("running pre gateway filter factory");

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                
            Optional.ofNullable(config.value).ifPresent(cookie -> {
                log.info("value cookeeiii: " + cookie);
                exchange.getResponse().addCookie(ResponseCookie.from(config.name, cookie).build());
            });

                log.info("running post gateway filter factory" + config.message);
            }));
        },100);
    }

    // inner class for ???
    public static class ConfigurationCookie {

        private String name;
        private String value;
        private String message;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
