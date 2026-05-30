package com.AquaBalance.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                            "http://localhost:4200",
                            "https://cerulean-kitten-f5af4b.netlify.app"
                    ));
                    config.setAllowedMethods(List.of(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                    ));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ── Públicas sin token ──────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // ── Usuarios: solo Admin ────────────────────────────
                        .requestMatchers("/api/usuarios/**")
                        .hasRole("Administrador")

                        // ── Notificaciones: Admin y Operador ────────────────
                        .requestMatchers("/api/notificaciones/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── Monitoreo (recursos, mediciones, contaminantes) ─
                        // GET: los 3 roles pueden leer
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/monitoreo/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")
                        // POST/PUT/DELETE: solo Admin y Operador
                        .requestMatchers("/api/monitoreo/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── Eventos ─────────────────────────────────────────
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/eventos/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")
                        .requestMatchers("/api/eventos/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── Alertas ─────────────────────────────────────────
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/alertas/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")
                        .requestMatchers("/api/alertas/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── Informes / Reportes ─────────────────────────────
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/informes/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")
                        .requestMatchers("/api/informes/**")
                        .hasAnyRole("Administrador", "Operador")

                        .requestMatchers("/api/reportes/**")
                        .hasRole("Administrador")

                        // ── Todo lo demás requiere estar autenticado ─────────
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}