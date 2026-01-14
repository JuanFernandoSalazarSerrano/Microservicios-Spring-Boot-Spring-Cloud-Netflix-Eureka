package com.fsalazar.springcloud.msvc.items;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Configuration
public class AppConfig {

    @Bean
    // here we are configuring the parameters for the states of our circuit with id items
    // circuitBreakerFactory.create("items").run(() -> service.findById(id))

    Customizer<Resilience4JCircuitBreakerFactory> customizerCircuitBreaker(){
        return (factory) -> factory.configureDefault(id -> {
            
            // default config
            // return new Resilience4JConfigBuilder(id).circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            
            return new Resilience4JConfigBuilder(id).circuitBreakerConfig(CircuitBreakerConfig
                .custom()
                .waitDurationInOpenState(Duration.ofSeconds(10L))
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .build())
                .build();
            }
        );
    }
}
