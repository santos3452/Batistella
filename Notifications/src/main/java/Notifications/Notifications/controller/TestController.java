package Notifications.Notifications.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Notifications.Notifications.util.ImageUtil;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final ImageUtil imageUtil;
    
    @Autowired
    public TestController(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }
    
    @GetMapping("/logo-info")
    public ResponseEntity<String> getLogoInfo() {
        StringBuilder info = new StringBuilder();
        
        // Probar la ruta principal
        String logoPath = "assets/images/Batistella.png";
        ClassPathResource resource = new ClassPathResource(logoPath);
        info.append("Ruta principal: ").append(logoPath).append("\n");
        info.append("¿Existe? ").append(resource.exists()).append("\n");
        
        if (resource.exists()) {
            try {
                info.append("Tamaño: ").append(resource.contentLength()).append(" bytes\n");
            } catch (IOException e) {
                info.append("Error al obtener tamaño: ").append(e.getMessage()).append("\n");
            }
        }
        
        // Probar la ruta alternativa
        String altPath = "static/images/Batistella.png";
        ClassPathResource altResource = new ClassPathResource(altPath);
        info.append("\nRuta alternativa: ").append(altPath).append("\n");
        info.append("¿Existe? ").append(altResource.exists()).append("\n");
        
        if (altResource.exists()) {
            try {
                info.append("Tamaño: ").append(altResource.contentLength()).append(" bytes\n");
            } catch (IOException e) {
                info.append("Error al obtener tamaño: ").append(e.getMessage()).append("\n");
            }
        }
        
        // Información sobre la conversión a base64
        info.append("\nConversión a base64:\n");
        String base64 = imageUtil.getImageAsBase64FromFilesystem("assets/images/Batistella.png");
        info.append("¿Se generó base64 desde filesystem? ").append(!base64.isEmpty()).append("\n");
        
        if (!base64.isEmpty()) {
            info.append("Longitud del base64: ").append(base64.length()).append(" caracteres\n");
            info.append("Primeros 100 caracteres: ").append(base64.substring(0, Math.min(100, base64.length()))).append("\n");
        }
        
        return ResponseEntity.ok(info.toString());
    }
    
    @GetMapping(value = "/logo", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getLogo() {
        try {
            Resource resource = new ClassPathResource("assets/images/Batistella.png");
            return ResponseEntity.ok().body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/logo-html")
    public ResponseEntity<String> getLogoHtml() {
        String base64 = imageUtil.getImageAsBase64FromFilesystem("assets/images/Batistella.png");
        
        String html = "<!DOCTYPE html><html><body>" +
            "<h1>Prueba de logo</h1>" +
            "<div>Usando base64:</div>" +
            "<img src='" + base64 + "' alt='Logo Base64' style='max-width:300px;'><br><br>" +
            "<div>Usando ruta estática:</div>" +
            "<img src='/images/Batistella.png' alt='Logo Estático' style='max-width:300px;'><br><br>" +
            "<div>Usando endpoint:</div>" +
            "<img src='/api/test/logo' alt='Logo Endpoint' style='max-width:300px;'>" +
            "</body></html>";
        
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
    }
} 