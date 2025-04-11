package com.example.UsersAndLogin.Controller;

import com.example.UsersAndLogin.Dto.UserDto;
import com.example.UsersAndLogin.Dto.UserResponseDto;
import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        try {
            userService.createUser(userDto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Usuario creado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<UserEntity> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            // Convertir a DTO de respuesta para no exponer la contraseña
            UserResponseDto responseDto = UserResponseDto.fromEntity(userOpt.get());
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No se encontró usuario con el email: " + email);
        }
    }
} 