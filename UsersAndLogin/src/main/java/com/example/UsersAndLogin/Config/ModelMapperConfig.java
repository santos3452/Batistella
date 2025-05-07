package com.example.UsersAndLogin.Config;

import com.example.UsersAndLogin.Dto.UpdateAdressDto;
import com.example.UsersAndLogin.Dto.UpdateUserDto;
import com.example.UsersAndLogin.Entity.DomicilioEntity;
import com.example.UsersAndLogin.Entity.UserEntity;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

        @Bean
        public ModelMapper modelMapper() {
            ModelMapper modelMapper = new ModelMapper();

            // Configuración para mapear correctamente entre DomicilioEntity y UpdateAdressDto
            modelMapper.createTypeMap(UpdateAdressDto.class, DomicilioEntity.class)
                    .addMappings(mapper -> {
                        // Configuraciones específicas si son necesarias
                    });

            // Configuración para mapear correctamente entre UserEntity y UpdateUserDto
            modelMapper.createTypeMap(UserEntity.class, UpdateUserDto.class)
                    .addMappings(mapper -> {
                        // No mapear la contraseña directamente por seguridad
                        mapper.skip(UpdateUserDto::setPassword);
                    });

            return modelMapper;
        }

}
