package com.example.Products.Config;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class GoogleCloudStorageConfig {

    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Value("${gcp.credentials.location:#{null}}")
    private String credentialsLocation;

    @Bean
    public Storage storage() throws IOException {
        StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder();

        // Si se especifica una ubicación de credenciales, las cargamos
        if (credentialsLocation != null && !credentialsLocation.isEmpty()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ClassPathResource(credentialsLocation).getInputStream());
            optionsBuilder.setCredentials(credentials);
        } else {
            // Si no hay credenciales específicas, intentamos usar las credenciales por defecto
            // Esto funcionará en entornos GCP o si la variable GOOGLE_APPLICATION_CREDENTIALS está configurada
            try {
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
                optionsBuilder.setCredentials(credentials);
            } catch (IOException e) {
                throw new RuntimeException("No se pudieron cargar las credenciales de Google Cloud. " +
                        "Configure gcp.credentials.location o la variable de entorno GOOGLE_APPLICATION_CREDENTIALS", e);
            }
        }

        return optionsBuilder.build().getService();
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }
} 