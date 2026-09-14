package com.fourati.app.security;

import com.fourati.app.config.WebConfig;

import com.fourati.app.config.AppProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import jakarta.annotation.PostConstruct;

/**
 * Stateless JWT resource-server security.
 *
 * CORS must be enabled here, not just in {@link WebConfig} — Spring Security's filter
 * chain runs before DispatcherServlet, so WebConfig's MVC-level CORS mapping never even
 * sees a request that Security's own authorizeHttpRequests() has already rejected. With
 * .cors(cors -> cors.disable()), every cross-origin OPTIONS preflight hit
 * .anyRequest().authenticated() and came back 401 before DispatcherServlet ever ran —
 * confirmed live: no authenticated cross-origin request from a real browser (e.g. the
 * admin frontend on a different port) could ever succeed, since the browser aborts the
 * real request when its preflight fails.
 *
 * Identity: a bearer token becomes an {@link AppJwtAuthenticationToken} through
 * {@link AppJwtAuthenticationConverter}, which derives authorities from the scope and
 * roles claims and resolves (provisioning if needed) the backend user exactly once
 * per request (ADR 0006). Every error the chain itself produces (401, 403) is an RFC
 * 9457 problem-details body (ADR 0005).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties appProperties;
    private final Environment environment;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final AppJwtAuthenticationConverter jwtAuthenticationConverter;
    private final ProblemAuthenticationEntryPoint authenticationEntryPoint;
    private final ProblemAccessDeniedHandler accessDeniedHandler;

    /**
     * A wildcard origin combined with allow-credentials reflects any Origin back with
     * credentials. Acceptable for local dev, never for prod: refuse to start.
     */
    @PostConstruct
    void rejectWildcardCorsInProd() {
        boolean prod = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
        boolean wildcard = appProperties.cors().allowedOrigins().stream().anyMatch(o -> o.contains("*"));
        if (prod && wildcard && appProperties.cors().allowCredentials()) {
            throw new IllegalStateException("app.cors.allowed-origins must list explicit origins in prod (found wildcard with allow-credentials=true)");
        }
    }

    private static final String[] PUBLIC_PATHS = {
        "/actuator/health",
        "/actuator/health/**",
        "/actuator/info",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        // SockJS's handshake (GET /ws/info, XHR-streaming, etc.) is plain HTTP and
        // can't carry a normal Authorization header the way REST calls do — real
        // auth for the WebSocket session happens at the STOMP CONNECT frame via
        // WebSocketAuthChannelInterceptor, not here.
        "/ws/**"
    };

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        AppProperties.Cors cors = appProperties.cors();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(cors.allowedOrigins());
        configuration.setAllowedMethods(cors.allowedMethods());
        configuration.setAllowedHeaders(cors.allowedHeaders());
        configuration.setAllowCredentials(cors.allowCredentials());
        configuration.setMaxAge(cors.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .contentTypeOptions(c -> {})
                .frameOptions(f -> f.deny())
                .httpStrictTransportSecurity(h -> h.includeSubDomains(true).maxAgeInSeconds(31_536_000))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicyHeader(p -> p.policy("camera=(), microphone=(), geolocation=()")))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Metrics scrape and SBOM: anonymous only when they arrive on the dedicated management
                // port, which is never exposed through the ingress (see deploy/k8s, docker-compose.yml).
                .requestMatchers(request -> isManagementPort(request.getLocalPort())
                        && (request.getRequestURI().startsWith("/actuator/prometheus")
                            || request.getRequestURI().startsWith("/actuator/sbom"))).permitAll()
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            // One error shape everywhere: the chain's own 401/403 are problem-details too.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
            // X-Api-Key callers are authenticated before the Bearer filter runs.
            .addFilterBefore(apiKeyAuthenticationFilter, BearerTokenAuthenticationFilter.class)
            .build();
    }

    /**
     * JWKS fetches get explicit connect/read timeouts so a slow or unreachable
     * Keycloak fails a request fast instead of holding a thread for the client
     * library's default. jwk-set-uri (docker profile) is honoured; otherwise the
     * decoder is built lazily from the issuer so startup does not require Keycloak.
     */
    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        RestTemplate rest = new RestTemplate(factory);
        rest.getInterceptors().add(new JwksCircuitBreaker());
        String issuer = props.getJwt().getIssuerUri();
        String jwkSetUri = props.getJwt().getJwkSetUri();
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).restOperations(rest).build();
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
            return decoder;
        }
        return new SupplierJwtDecoder(() -> NimbusJwtDecoder.withIssuerLocation(issuer).restOperations(rest).build());
    }

    private boolean isManagementPort(int localPort) {
        String management = environment.getProperty("management.server.port");
        String server = environment.getProperty("server.port", "8080");
        return management != null && !management.equals(server) && management.equals(Integer.toString(localPort));
    }
}
