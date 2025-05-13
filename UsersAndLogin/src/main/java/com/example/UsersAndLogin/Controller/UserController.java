package com.example.UsersAndLogin.Controller;

import com.example.UsersAndLogin.Dto.Error.ErrorDto;
import com.example.UsersAndLogin.Dto.UpdateAdressDto;
import com.example.UsersAndLogin.Dto.UpdateUserDto;
import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Dto.UserResponseDto;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Security.JwtUtils;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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

    @GetMapping("/getUserByID/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getUserByID(@PathVariable Long id) {

        try{
            UserDto userOpt = userService.getUserById(id);
            return ResponseEntity.ok(userOpt);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorDto.of(
                            HttpStatus.NOT_FOUND.value(),
                            "Usuario No Encontrado",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al obtener el usuario: " + e.getMessage()
                    ));
        }




    }




    @DeleteMapping("/deleteAdress")
    // Temporalmente desactivamos el requisito de token para pruebas
    public ResponseEntity<?> deleteAdress(@RequestParam Long id){
        try {

            try {
                userService.deleteAdress(id);
                return ResponseEntity.ok("Dirección eliminada exitosamente");
            } catch (IllegalArgumentException e) {
                // Este error puede ocurrir si el domicilio no existe
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ErrorDto.of(
                                HttpStatus.NOT_FOUND.value(),
                                "Domicilio No Encontrado",
                                e.getMessage()
                        ));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Agregar para depuración
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al eliminar la dirección: " + e.getMessage()
                    ));
        }
    }

    @PutMapping("/updateUser")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserDto requestDto) {
        try {
            // Obtener el usuario autenticado del token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailAutenticado = authentication.getName();

            // Obtener los datos del usuario actual
            UserEntity usuarioActual = userService.findByEmail(emailAutenticado)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            // Sobrescribir el correo en el DTO con el del usuario autenticado para evitar suplantación
            requestDto.setMail(emailAutenticado);

            // Intentar actualizar el usuario con los nuevos datos
            UpdateUserDto updatedUser = userService.UpdateUser(requestDto);
            return ResponseEntity.ok(updatedUser);
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
                            "Error Interno",
                            "Error al actualizar el usuario: " + e.getMessage()
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

    // Método OPTIONS para manejar las solicitudes de preflight CORS
    @RequestMapping(value = "/deleteAdress", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptionsDomicilio() {
        return ResponseEntity
                .ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .build();
    }
} 