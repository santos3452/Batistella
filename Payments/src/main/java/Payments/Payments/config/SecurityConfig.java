package Payments.Payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("!disable-security")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Permitir acceso a endpoints públicos
                        .requestMatchers("/api/pagos/webhooks/**").permitAll()
                        .requestMatchers("/api/pagos/estado/**").permitAll()
                        .requestMatchers("/api/pagos/mercadopago/**").permitAll()
                        .requestMatchers("/api/pagos/retorno/**").permitAll()
                        .requestMatchers("/api/pagos/redirect/**").permitAll()
                        .requestMatchers("/api/pagos/diagnostico").permitAll()
                        .requestMatchers("/api/pagos/manual").permitAll()
                        .requestMatchers("/api/pagos/transferencia").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/pagos/**").permitAll()
                        .requestMatchers("/redirect.html").permitAll()
                        .requestMatchers("/index.html").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        // Permitir acceso a Swagger
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        // Requiere autenticación y rol ADMINISTRADOR para el endpoint de cambiar estado
                        .requestMatchers("/api/pagos/cambiarEstado").hasAnyAuthority("ADMINISTRADOR", "ADMIN", "ROLE_ADMIN")
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
} 