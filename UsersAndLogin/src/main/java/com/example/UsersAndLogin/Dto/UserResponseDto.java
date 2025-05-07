package com.example.UsersAndLogin.Dto;

import com.example.UsersAndLogin.Entity.UserEntity;
import com.example.UsersAndLogin.Entity.enums.Role;
import com.example.UsersAndLogin.Entity.enums.UserType;
import com.example.UsersAndLogin.Dto.UpdateAdressDto;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Role rol;
    private UserType tipoUsuario;
    private List<UpdateAdressDto> domicilio;
    
    // Constructor para convertir desde la entidad
    public static UserResponseDto fromEntity(UserEntity entity) {
        // Mapear lista de domicilios
        var domiciliosDto = entity.getDomicilio() == null ? Collections.emptyList() :
            entity.getDomicilio().stream()
                .map(d -> {
                    UpdateAdressDto dto = new UpdateAdressDto();
                    dto.setId(d.getId());
                    dto.setCalle(d.getCalle());
                    dto.setNumero(d.getNumero());
                    dto.setCiudad(d.getCiudad());
                    dto.setCodigoPostal(d.getCodigoPostal());
                    return dto;
                })
                .collect(Collectors.toList());
        return UserResponseDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .email(entity.getEmail())
                .rol(entity.getRol())
                .tipoUsuario(entity.getTipoUsuario())
                .domicilio((List<UpdateAdressDto>) domiciliosDto)
                .build();
    }
} 