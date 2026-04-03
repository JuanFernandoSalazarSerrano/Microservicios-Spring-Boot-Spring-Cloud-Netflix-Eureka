package com.andres.springcloud.msvc.oauth.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
// This class configures your Authorization Server.
// It is responsible for login, issuing JWT access tokens, and publishing signing keys.
public class SecurityConfig {

        private PasswordEncoder passwordEncoder;        

        public SecurityConfig(PasswordEncoder passwordEncoder) {
                this.passwordEncoder = passwordEncoder;
        }

        @Bean
        @Order(1)
        // Filter chain #1: special endpoints used by OAuth2 Authorization Server
        // (/oauth2/authorize, /oauth2/token, /.well-known/jwks.json, etc.).
        SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
                        throws Exception {
                // Applies Spring Authorization Server default security configuration.
                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .oidc(Customizer.withDefaults()); // Enable OpenID Connect 1.0
                http
                                // Redirect to the login page when not authenticated from the
                                // authorization endpoint
                                .exceptionHandling((exceptions) -> exceptions
                                                .defaultAuthenticationEntryPointFor(
                                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                                // Accept access tokens for User Info and/or Client Registration
                                .oauth2ResourceServer((resourceServer) -> resourceServer
                                                .jwt(Customizer.withDefaults()));

                return http.build();
        }

        @Bean
        @Order(2)
        // Filter chain #2: fallback security for any other endpoint in this service.
        SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
                        throws Exception {
                http
                                .authorizeHttpRequests((authorize) -> authorize
                                                .anyRequest().authenticated())
                                // Form login handles the redirect to the login page from the
                                // authorization server filter chain
                                .csrf(csrf -> csrf.disable())
                                .formLogin(Customizer.withDefaults());

                return http.build();
        }

        // @Bean
        // // Demo users stored in memory.
        // // These users authenticate in the auth server login page.
        // UserDetailsService userDetailsService() {
        //         UserDetails userDetails = User.builder()
        //                         .username("andres")
        //                         .password("{noop}12345")
        //                         .roles("USER")
        //                         .build();
        //         UserDetails admin = User.builder()
        //                         .username("admin")
        //                         .password("{noop}12345")
        //                         .roles("USER", "ADMIN")
        //                         .build();

        //         return new InMemoryUserDetailsManager(userDetails, admin);
        // }

        @Bean
        // OAuth2 client registration (the application allowed to request tokens).
        // Here the client is your gateway (clientId: gateway-app).
        RegisteredClientRepository registeredClientRepository() {
                RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("gateway-app")
                                .clientSecret(passwordEncoder.encode("12345"))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri("http://127.0.0.1:8090/login/oauth2/code/client-app")
                                .redirectUri("http://127.0.0.1:8090/authorized")
                                .postLogoutRedirectUri("http://127.0.0.1:8090/logout")
                                .scope(OidcScopes.OPENID)
                                .scope(OidcScopes.PROFILE)
                                // Disable consent screen for simplicity in local development.
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .build();

                return new InMemoryRegisteredClientRepository(oidcClient);
        }

        @Bean
        // Exposes the signing key set (JWK Source) used to sign JWT tokens.
        // Resource servers (like msvc-gateway-server) use this public key to verify tokens.
        JWKSource<SecurityContext> jwkSource() {
                KeyPair keyPair = generateRsaKey();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                RSAKey rsaKey = new RSAKey.Builder(publicKey)
                                .privateKey(privateKey)
                                .keyID(UUID.randomUUID().toString())
                                .build();
                JWKSet jwkSet = new JWKSet(rsaKey);
                return new ImmutableJWKSet<>(jwkSet);
        }

        // Utility method to create an RSA key pair.
        // Private key signs JWTs; public key validates them.
        private static KeyPair generateRsaKey() {
                KeyPair keyPair;
                try {
                        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                        keyPairGenerator.initialize(2048);
                        keyPair = keyPairGenerator.generateKeyPair();
                } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                }
                return keyPair;
        }

        @Bean
        // Decoder used internally by Spring Security for JWT handling in this service.
        JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
                return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        }

        @Bean
        // Default Authorization Server settings (issuer/endpoints can be customized here if needed).
        AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder().build();
        }
        

        // Adds custom claims to ACCESS_TOKEN before it is signed.
        // This is the key integration point with your gateway security rules.
        // Gateway reads "roles" claim and converts it to authorities.
        @Bean
        OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
                return context -> {
                        if (context.getTokenType().getValue() == OAuth2TokenType.ACCESS_TOKEN.getValue()) {
                                // Logged-in user (andres/admin) represented as Authentication principal.
                                Authentication principal = context.getPrincipal();
                                context.getClaims()
                                                // Example custom business claim.
                                                .claim("data", "data adicional en el token")
                                                // Export authorities (ROLE_USER, ROLE_ADMIN, etc.) to JWT claim.
                                                // Gateway later maps this to GrantedAuthority objects.
                                                .claim("roles", principal.getAuthorities()
                                                                .stream()
                                                                .map(GrantedAuthority::getAuthority)
                                                                .collect(Collectors.toList()));
                        }
                };
        }

}
