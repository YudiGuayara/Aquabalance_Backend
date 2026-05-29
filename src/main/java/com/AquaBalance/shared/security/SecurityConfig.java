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
                            "https://aquabalance-frontend.vercel.app/"
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

                        // ── PÚBLICO ──────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/notificaciones/**").permitAll()

                        // ── USUARIOS — solo Administrador ─────────────────
                        .requestMatchers("/api/usuarios/**")
                        .hasRole("Administrador")

                        // ── MONITOREO GET — todos los roles autenticados ──
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/monitoreo/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")

                        // ── MONITOREO POST/PUT/DELETE — solo Admin y Operador
                        .requestMatchers("/api/monitoreo/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── EVENTOS GET — todos los roles autenticados ────
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/eventos/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")

                        // ── EVENTOS POST/PUT/DELETE — solo Admin y Operador
                        .requestMatchers("/api/eventos/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── ALERTAS GET — todos los roles autenticados ────
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/alertas/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")

                        // ── ALERTAS POST/PUT/DELETE — solo Admin y Operador
                        .requestMatchers("/api/alertas/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── INFORMES GET — todos los roles autenticados ───
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/informes/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")

                        // ── INFORMES POST/PUT/DELETE — solo Admin y Operador
                        .requestMatchers("/api/informes/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── REPORTES — solo Administrador ─────────────────
                        .requestMatchers("/api/reportes/**")
                        .hasRole("Administrador")

                        // ── TODO LO DEMÁS requiere autenticación ──────────
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