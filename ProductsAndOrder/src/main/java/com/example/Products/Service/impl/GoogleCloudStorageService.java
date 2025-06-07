package com.example.Products.Service.impl;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GoogleCloudStorageService {

    private final Storage storage;
    private final String bucketName;

    @Value("${gcp.image.url.expiration:86400}")
    private Long urlExpirationSeconds;

    @Autowired
    public GoogleCloudStorageService(Storage storage, String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    /**
     * Sube una imagen a Google Cloud Storage
     * 
     * @param file Archivo a subir
     * @param folderName Nombre de la carpeta (opcional)
     * @param fileName Nombre del archivo (opcional)
     * @return URL firmada para acceder a la imagen
     * @throws IOException Si hay un error al procesar el archivo
     */
    public String uploadImage(MultipartFile file, String folderName, String fileName) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("El archivo está vacío o es nulo");
        }

        // Si no se proporciona un nombre de archivo, genera uno usando UUID
        if (fileName == null || fileName.trim().isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            fileName = UUID.randomUUID().toString() + extension;
        }

        // Construir la ruta del objeto
        String objectName = (folderName != null && !folderName.trim().isEmpty())
                ? folderName + "/" + fileName
                : fileName;

        // Crear el BlobId y BlobInfo
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        // Subir el archivo
        storage.create(blobInfo, file.getBytes());

        // Generar URL firmada para acceso
        URL signedUrl = storage.signUrl(
                blobInfo,
                urlExpirationSeconds, 
                TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature()
        );

        return signedUrl.toString();
    }

    /**
     * Elimina una imagen de Google Cloud Storage
     * 
     * @param imageUrl URL de la imagen a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extraer el nombre del objeto de la URL
            String objectName = extractObjectNameFromUrl(imageUrl);
            if (objectName == null) {
                return false;
            }

            // Eliminar el objeto
            BlobId blobId = BlobId.of(bucketName, objectName);
            return storage.delete(blobId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extrae el nombre del objeto de la URL de Google Cloud Storage
     * 
     * @param imageUrl URL de la imagen
     * @return Nombre del objeto
     */
    private String extractObjectNameFromUrl(String imageUrl) {
        try {
            // La URL tiene este formato: https://storage.googleapis.com/{bucket-name}/{object-name}
            if (imageUrl != null && imageUrl.contains(bucketName)) {
                int startIndex = imageUrl.indexOf(bucketName) + bucketName.length() + 1;
                // Verificar si hay parámetros en la URL (signo de interrogación)
                int endIndex = imageUrl.indexOf('?');
                if (endIndex == -1) {
                    endIndex = imageUrl.length();
                }
                return imageUrl.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
} 