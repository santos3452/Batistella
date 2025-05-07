package com.example.UsersAndLogin.Entity;

import com.example.UsersAndLogin.Entity.enums.Role;
import com.example.UsersAndLogin.Entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;

    @Column(unique = true)
    private String email;
    
    private String password;

    @Enumerated(EnumType.STRING)
    private Role rol;

    @Enumerated(EnumType.STRING)
    private UserType tipoUsuario;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DomicilioEntity> domicilio;

    /**
     * Getter explícito para la lista de domicilios (lombok genera uno, pero lo definimos para evitar errores de compilación estática)
     */
    public List<DomicilioEntity> getDomicilio() {
        return this.domicilio;
    }

    @Column(nullable = false)
    private Boolean activo = true;
}
