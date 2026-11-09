package com.AquaBalance.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${sensores.api-key:}")
    private String sensoresApiKey;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http
                .csrf(AbstractHttpConfigurer::disable)

                // CORS: una sola configuracion, en CorsConfig
                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ── Públicas sin token ──────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/network/**").permitAll()

                        // Spring redirige internamente los errores (403, 404, 500) a /error;
                        // si no es publico, un 403 llega al cliente como 401 y cierra la sesion
                        .requestMatchers("/error").permitAll()

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

                        // ── Estaciones de red (sensores) ────────────────────
                        // Telemetria: la puede enviar un Admin/Operador (JWT) o un sensor real (X-API-Key)
                        .requestMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/estaciones/*/telemetria", "/api/estaciones/*/perdida")
                        .hasAnyRole("Administrador", "Operador", "SENSOR")
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/estaciones/**")
                        .hasAnyRole("Administrador", "Operador", "UsuarioPublico")
                        .requestMatchers("/api/estaciones/**")
                        .hasAnyRole("Administrador", "Operador")

                        // ── Todo lo demás requiere estar autenticado ─────────
                        .anyRequest().authenticated()
                )

                // Sin autenticación válida → 401 (así el frontend sabe que debe volver al login).
                // Con sesión pero sin permiso de rol → sigue siendo 403.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        new ApiKeyAuthenticationFilter(sensoresApiKey),
                        UsernamePasswordAuthenticationFilter.class)

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