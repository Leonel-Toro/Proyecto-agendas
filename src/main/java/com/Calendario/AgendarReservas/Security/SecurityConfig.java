package com.Calendario.AgendarReservas.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .headers(h -> h
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'"))
                .frameOptions(f -> f.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
        )
        .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/reservas/agendar").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reservas/lista/medios_pago").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reservas/historial").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reservas/historial/detalle/{id}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/reservas/editar").permitAll()
                .anyRequest().denyAll()
        );
        return http.build();
    }
}
