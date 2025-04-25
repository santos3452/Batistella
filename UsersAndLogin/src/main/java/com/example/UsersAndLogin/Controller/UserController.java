package com.example.UsersAndLogin.Controller;

import com.example.UsersAndLogin.Dto.Error.ErrorDto;
import com.example.UsersAndLogin.Dto.UpdateUserDto;
import com.example.UsersAndLogin.Dto.UserResponseDto;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Security.JwtUtils;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<UserEntity> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            UserResponseDto responseDto = UserResponseDto.fromEntity(userOpt.get());
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorDto.of(
                            HttpStatus.NOT_FOUND.value(),
                            "Usuario No Encontrado",
                            "No se encontró usuario con el email: " + email
                    ));
        }
    }

    @PostMapping("/updateUser")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateUser(@RequestParam String mail, 
                                      @RequestParam(required = false) String password, 
                                      @RequestParam String nombre, 
                                      @RequestParam String apellido) {
        try {
            // Obtener el usuario autenticado del token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailAutenticado = authentication.getName();
            

            
            // Verificar que el usuario solo pueda actualizar sus propios datos
            if (!emailAutenticado.equals(mail)) {
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ErrorDto.of(
                        HttpStatus.FORBIDDEN.value(),
                        "Error de Autorización",
                        "No tienes permiso para actualizar los datos de otro usuario"
                    ));
            }

            UpdateUserDto updatedUser = userService.UpdateUser(mail, password, nombre, apellido);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Usuario actualizado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        }
    }

    @PostMapping("/change-password")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            System.out.println("Iniciando cambio de contraseña");
            
            // Obtener el token del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            System.out.println("Email del usuario autenticado: " + email);
            
            // Intentar cambiar la contraseña
            userService.changePassword(email, oldPassword, newPassword);
            
            return ResponseEntity.ok("Contraseña actualizada exitosamente");
        } catch (IllegalArgumentException e) {
            System.out.println("Error de validación: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error de Validación",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al cambiar la contraseña: " + e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/deleteUser")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteUser() {
        try {
            // Obtener el usuario autenticado del token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailAutenticado = authentication.getName();
            
            System.out.println("Iniciando baja lógica para usuario: " + emailAutenticado);
            
            // Realizar la baja lógica
            userService.deleteUser(emailAutenticado);
            
            return ResponseEntity.ok("Usuario dado de baja exitosamente");
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
                            "Error al dar de baja el usuario: " + e.getMessage()
                    ));
        }
    }
} 