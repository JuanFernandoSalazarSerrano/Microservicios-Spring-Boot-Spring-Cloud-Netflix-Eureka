package com.fsalazar.springcloud.app.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security Configuration for the API Gateway Server.
 * 
 * This class configures Spring Security to:
 * 1. Control which endpoints require authentication and which are public
 * 2. Enable OAuth2 login (users can login via an OAuth2 provider like our auth server)
 * 3. Enable OAuth2 client capabilities (the gateway can call other services using OAuth2 tokens)
 * 4. Validate JWT tokens (resource server mode - the gateway validates tokens from the auth server)
 * 
 * Note: This uses ServerHttpSecurity (reactive/WebFlux) instead of HttpSecurity (servlet-based).
 * This is for non-blocking, high-performance request handling.
 */
@Configuration
public class SecurityConfig {

    /**
     * SecurityWebFilterChain bean defines all the security rules for the gateway.
     * 
     * Think of this as a "filter" that runs on every HTTP request and decides:
     * - Is this request allowed without login?
     * - Does this request need authentication?
     * - How does the user prove they are authenticated (OAuth2 token, JWT, etc)?
     * 
     * @param http The reactive security configuration object
     * @return A SecurityWebFilterChain that applies all the rules
     */
    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
            // Step 1: Define URL patterns and what roles/authentication they require
            .authorizeExchange(authz -> authz
                // PUBLIC ENDPOINTS (no login required)
                
                // The "/authorized" endpoint is completely open to everyone
                // Used for health checks or public status endpoints
                .pathMatchers("/authorized")
                    .permitAll() // permitAll() allows requests without any authentication
                
                // GET requests to list items, products, users are public (no auth required)
                // Common pattern: GET LIST operations are public for discovery
                .pathMatchers(HttpMethod.GET, "/api/items", "api/producs", "api/users")
                    .permitAll()
                
                // PROTECTED ENDPOINTS (login required, specific roles allowed)
                
                // GET specific item/product/user by ID requires either ADMIN or USER role
                // Common pattern: view a single resource requires authentication
                .pathMatchers(HttpMethod.GET, "/api/items/{id}", "/api/products/{id}", "/api/users/{id}")
                    .hasAnyRole("ADMIN", "USER")
                
                // ALL other requests to /api/products/**, /api/items/**, /api/users/** (POST, PUT, DELETE, etc)
                // require ADMIN role. This protects modifying operations (create, update, delete)
                .pathMatchers("/api/products/**", "/api/items/**", "/api/users/**")
                    .hasRole("ADMIN")
                
                // Any other request (.anyExchange()) must be authenticated
                // (user must be logged in, but doesn't check specific roles)
                .anyExchange()
                    .authenticated()
            )
            // Step 2: Disable CSRF protection
            // CSRF is not needed for APIs because:
            // 1. APIs use tokens (JWT in Authorization header), not cookies
            // 2. Tokens are harder to forge than cookie-based sessions
            .cors(cors -> cors.disable())
            
            // Step 3: Enable OAuth2 Login (user-facing login flow)
            // When a user visits a protected page without a token:
            // 1. Spring redirects them to the OAuth2 login page (our auth server)
            // 2. User logs in at the auth server
            // 3. Auth server redirects them back with an authorization code
            // 4. Gateway exchanges code for a token
            // 5. Gateway creates a session for the user
            // withDefaults() = use Spring's default OAuth2 login configuration
            .oauth2Login(withDefaults())
            
            // Step 4: Enable OAuth2 Client (gateway can call other services)
            // The gateway can now use OAuth2 to authenticate when calling downstream services
            // Example: gateway calls /api/items, which calls another microservice
            // The gateway will automatically include the OAuth2 token when calling that service
            .oauth2Client(withDefaults())
            
            // Step 5: Enable JWT validation (resource server mode)
            // The gateway validates JWT tokens sent in the Authorization header
            // Flow: 
            // 1. Client sends: Authorization: Bearer <JWT_TOKEN>
            // 2. Gateway extracts the token
            // 3. Gateway validates it (signature, expiration, etc)
            // 4. If valid, request is allowed; if invalid, request is rejected
            // This is how API clients (mobile apps, SPAs, etc) authenticate
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
            
            // Build and return the security filter chain
            // This filter will be applied to all HTTP requests
            .build();
    }

}


