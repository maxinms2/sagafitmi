package com.sagafitmi.ecommerce.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

import com.sagafitmi.ecommerce.service.AllowedOriginService;

import com.sagafitmi.ecommerce.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Chain mínima por ahora: deshabilita CSRF y permite todas las peticiones.
     * Esto evita que la aplicación quede bloqueada al añadir el starter de Security.
     * Más adelante cambiaremos las reglas para exigir autenticación y JWT.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, @Qualifier("corsConfigurationSource") CorsConfigurationSource corsSource) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsSource))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Product images: only ADMIN can access any images endpoints
                .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/images/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/images/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,"/api/images/**").hasRole("ADMIN")
                .requestMatchers("/images/**").permitAll()
                // Products: allow read for everyone (list, detail, search), restrict write/update/delete to ADMIN
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*", "/api/products/search").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/cart/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.DELETE, "/api/cart/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/cart/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.GET, "/api/cart/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")

                // Users: only ADMIN can access any user endpoints
                // Allow account creation without authentication
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                // Allow lookup by email for authenticated users (ADMIN or USER)
                .requestMatchers(HttpMethod.GET, "/api/users/by-email").hasAnyRole("ADMIN", "USER")
                // Other user endpoints require ADMIN
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // Metrics: only ADMIN can access metrics endpoints
                .requestMatchers("/api/metrics/**").hasRole("ADMIN")

                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Registrar el filtro JWT antes del UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(AllowedOriginService allowedOriginService) {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = allowedOriginService.getAllowedOrigins();
        if (origins == null || origins.isEmpty()) {
            // Fallback a localhost:5173 si la tabla está vacía
            origins = List.of("http://localhost:5173");
        }

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
