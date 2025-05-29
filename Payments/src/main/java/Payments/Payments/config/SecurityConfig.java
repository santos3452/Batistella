package Payments.Payments.config;

import java.util.Arrays;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("!disable-security")
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configurando SecurityFilterChain con JwtAuthenticationFilter");
        
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Permitir acceso a endpoints públicos
                        .requestMatchers("/api/pagos/webhooks/**").permitAll()
                        .requestMatchers("/api/pagos/estado/**").permitAll()
                        .requestMatchers("/api/pagos/mercadopago/**").permitAll() // Comentar para requerir token
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
                        // Requiere autenticación y rol ADMINISTRADOR para el endpoint de cambiar estado
                        .requestMatchers("/api/pagos/cambiarEstado").hasAnyAuthority("ADMINISTRADOR", "ADMIN", "ROLE_ADMIN")
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 