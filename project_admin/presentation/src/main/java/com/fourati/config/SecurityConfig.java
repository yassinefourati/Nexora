package com.fourati.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * real request when its preflight fails. Authorities are derived from both the standard
 * "scope"/"scp" claims and the "roles" claim (as populated by the API's own role
 * model), so method security (@PreAuthorize("hasRole('admin')")) works against
 * API roles.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties appProperties;

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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthorityPrefix("SCOPE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<org.springframework.security.core.GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);
            List<String> roles = jwt.getClaimAsStringList("roles");
            Stream<org.springframework.security.core.GrantedAuthority> roleAuthorities = roles == null
                ? Stream.empty()
                : roles.stream().map(role -> (org.springframework.security.core.GrantedAuthority)
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            return Stream.concat(scopeAuthorities.stream(), roleAuthorities).collect(Collectors.toSet());
        });
        return converter;
    }
}
