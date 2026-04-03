package com.andres.springcloud.app.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
// This class defines security rules for the API Gateway (reactive stack / WebFlux).
// The gateway validates JWTs issued by your OAuth server and decides who can access each route.
public class SecurityConfig {

    @Bean
    // Spring creates this bean at startup and uses it as the security pipeline for all requests.
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {

        // authorizeExchange = route-based authorization rules.
        // Rules are evaluated in order: first match wins.
        return http.authorizeExchange(authz -> {
            // Public endpoints used during login/logout redirection flow.
            authz.pathMatchers("/authorized", "/logout").permitAll()
                    // Public read-only access for list endpoints.
                    .pathMatchers(HttpMethod.GET, "/api/items", "/api/products", "/api/users").permitAll()
                    // Reading a single resource by id requires either USER or ADMIN role.
                    .pathMatchers(HttpMethod.GET, "/api/items/{id}", "/api/products/{id}", "/api/users/{id}")
                    .hasAnyRole("ADMIN", "USER")
                    // Any other operation under these APIs (POST/PUT/DELETE, etc.) requires ADMIN.
                    .pathMatchers("/api/items/**", "/api/products/**", "/api/users/**").hasRole("ADMIN")
                    // Fallback rule: everything else must be authenticated.
                    .anyExchange().authenticated();
        })
                // CORS is disabled here (for local/demo setups). In production, configure CORS explicitly.
                .cors(csrf -> csrf.disable())
                // Enables OAuth2 login (browser-based authorization code flow).
                .oauth2Login(withDefaults())
                // Enables this gateway acting as an OAuth2 client when needed.
                .oauth2Client(withDefaults())
                // Enables JWT Bearer token validation for API/resource server behavior.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        // Custom converter maps JWT claims -> Spring Security authorities.
                        jwt -> jwt.jwtAuthenticationConverter(new Converter<Jwt, Mono<AbstractAuthenticationToken>>() {

                            @Override
                            public Mono<AbstractAuthenticationToken> convert(Jwt source) {
                                // Read custom "roles" claim from token.
                                // This claim is added by msvc-oauth in its tokenCustomizer() bean.
                                Collection<String> roles = source.getClaimAsStringList("roles");

                                // Convert each role string (ex: ROLE_ADMIN) into a GrantedAuthority.
                                Collection<GrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());

                                // Build authenticated principal using JWT + mapped authorities.
                                // These authorities are what hasRole()/hasAnyRole() checks above use.
                                return Mono.just(new JwtAuthenticationToken(source, authorities));
                            }

                        })))
                .build();
    }
}
