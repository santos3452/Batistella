package Notifications.Notifications.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        // Propiedades para evitar problemas de codificación
        props.put("mail.smtp.allow8bitmime", "true");
        props.put("mail.smtps.allow8bitmime", "true");
        props.put("mail.mime.charset", "UTF-8");
        props.put("mail.mime.encodeparameters", "false");
        props.put("mail.mime.encodefilename", "false");
        props.put("mail.mime.decodetext.strict", "false");
        props.put("mail.mime.qp.ignoreonerror", "true");
        props.put("mail.mime.base64.ignoreonerror", "true");
        props.put("mail.mime.splitlongparameters", "false");
        
        // Configurar el debug para ver los problemas
        props.put("mail.debug", "true");
        
        return mailSender;
    }
} 