package com.AquaBalance.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 🌍 FRONTEND EN PRODUCCIÓN (Vercel)
        config.setAllowedOrigins(List.of(
                "https://aquabalance-frontend.vercel.app"
        ));

        // 🔓 Métodos permitidos
        config.addAllowedMethod("*");

        // 🔓 Headers permitidos
        config.addAllowedHeader("*");

        // 🔐 Si usas JWT o cookies
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}