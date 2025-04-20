package com.example.UsersAndLogin.Controller;

import com.example.UsersAndLogin.Dto.AuthRequest;
import com.example.UsersAndLogin.Dto.AuthResponse;
import com.example.UsersAndLogin.Dto.Error.ErrorDto;
import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Security.CustomUserDetailsService;
import com.example.UsersAndLogin.Security.JwtUtils;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {
    private final AuthenticationManager authManager;
    private final CustomUserDetailsService uds;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager, CustomUserDetailsService uds, JwtUtils jwtUtils, UserService userService) {
        this.authManager = authManager;
        this.uds = uds;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(token));
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
            userService.createUser(userDto);
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
}
