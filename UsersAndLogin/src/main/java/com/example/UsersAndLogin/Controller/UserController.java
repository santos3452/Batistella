package com.example.UsersAndLogin.Controller;

import com.example.UsersAndLogin.Dto.Error.ErrorDto;
import com.example.UsersAndLogin.Dto.UpdateUserDto;
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
    public ResponseEntity<?> updateUser(@RequestParam String mail, 
                                      @RequestParam(required = false) String password, 
                                      @RequestParam String nombre, 
                                      @RequestParam String apellido) {
        try {
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
} 