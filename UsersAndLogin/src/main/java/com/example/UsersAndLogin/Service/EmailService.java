package com.example.UsersAndLogin.Service;

public interface EmailService {
    /**
     * Envía un email para restablecer la contraseña
     * @param to Email del destinatario
     * @param resetToken Token de restablecimiento
     */
    void sendPasswordResetEmail(String to, String resetToken);
} 