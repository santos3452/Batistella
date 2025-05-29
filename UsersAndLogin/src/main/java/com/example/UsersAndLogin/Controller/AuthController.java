package com.example.UsersAndLogin.Controller;

import com.example.UsersAndLogin.Dto.AuthRequest;
import com.example.UsersAndLogin.Dto.AuthResponse;
import com.example.UsersAndLogin.Dto.Error.ErrorDto;
import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Security.CustomUserDetailsService;
import com.example.UsersAndLogin.Security.JwtUtils;
import com.example.UsersAndLogin.Service.UserService;
import com.example.UsersAndLogin.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {
    private final AuthenticationManager authManager;
    private final CustomUserDetailsService uds;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    
    @Value("${notification.service.url:http://localhost:8085}")
    private String notificationServiceUrl;

    public AuthController(AuthenticationManager authManager, CustomUserDetailsService uds, JwtUtils jwtUtils, UserService userService, PasswordEncoder passwordEncoder, EmailService emailService, RestTemplate restTemplate) {
        this.authManager = authManager;
        this.uds = uds;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Verificar si el usuario existe y está activo antes de intentar autenticar
            UserEntity user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            
            if (!user.getActivo()) {
                throw new UsernameNotFoundException("Esta cuenta está dada de baja");
            }

            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (UsernameNotFoundException e) {
            String message = e.getMessage();
            if (message.contains("desactivada")) {
                message = "Esta cuenta está dada de baja";
            }
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorDto.of(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Error de Autenticación",
                            message
                    ));
        } catch (AuthenticationException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorDto.of(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Error de Autenticación",
                            "Credenciales inválidas"
                    ));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
        try {
            // Registrar el usuario
            UserEntity createdUser = userService.createUser(userDto);
            
            // Notificar al servicio de notificaciones
            try {
                // Acceder a los campos a través de los getters generados por Lombok
                String email = createdUser.getEmail();
                String name = createdUser.getNombre();
                
                // Construir la URL sin codificar manualmente los parámetros
                // UriComponentsBuilder codificará automáticamente los parámetros
                String url = UriComponentsBuilder.fromHttpUrl(notificationServiceUrl)
                        .path("/api/notifications/user/register/html")
                        .queryParam("email", email)
                        .queryParam("name", name)
                        .toUriString();
                
                System.out.println("Enviando notificación de registro a: " + url);
                
                // Realizar la llamada GET al servicio externo
                restTemplate.getForEntity(url, String.class);
                
                // No esperamos la respuesta para no bloquear el registro
            } catch (Exception e) {
                // Solo loguear el error, no afecta el flujo principal
                System.err.println("Error al enviar notificación de registro: " + e.getMessage());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al registrar usuario: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/reactivate")
    public ResponseEntity<?> reactivateAccount(@RequestParam String email) {
        try {
            userService.reactivateUser(email);
            return ResponseEntity.ok("Cuenta reactivada exitosamente. Por favor, inicie sesión.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al reactivar la cuenta: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            // Verificar si el usuario existe y está activo
            UserEntity user = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("No existe una cuenta con ese email"));

            if (!user.getActivo()) {
                throw new IllegalArgumentException("Esta cuenta está dada de baja");
            }

            // Generar token de restablecimiento (válido por 1 hora)
            String resetToken = jwtUtils.generatePasswordResetToken(email);

            // Enviar email
            emailService.sendPasswordResetEmail(email, resetToken);

            return ResponseEntity.ok("Se ha enviado un enlace de restablecimiento a tu correo electrónico");
        } catch (IllegalArgumentException e) {
            // No revelar si el email existe o no por seguridad
            return ResponseEntity.ok("Si existe una cuenta con ese email, recibirás un enlace para restablecer tu contraseña");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al procesar la solicitud de restablecimiento"
                    ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            // Extraer email del token y validar
            String email = jwtUtils.extractEmailFromResetToken(token);
            
            // Buscar usuario
            UserEntity user = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            if (!user.getActivo()) {
                throw new IllegalArgumentException("Esta cuenta está dada de baja");
            }

            // Actualizar contraseña
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);

            return ResponseEntity.ok("Contraseña actualizada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al restablecer la contraseña"
                    ));
        }
    }

    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        try {
            emailService.sendPasswordResetEmail(email, "test-token");
            return ResponseEntity.ok("Email enviado exitosamente");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error al enviar email",
                            e.getMessage()
                    ));
        }
    }
}
