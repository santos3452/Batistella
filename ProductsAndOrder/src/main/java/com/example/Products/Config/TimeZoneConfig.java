package com.example.Products.Config;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        // Establecer la zona horaria de Argentina para toda la aplicación
        TimeZone.setDefault(TimeZone.getTimeZone("America/Buenos_Aires"));
    }
} 