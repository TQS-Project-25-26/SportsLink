package tqs.sportslink.service;


import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String publicUrl;  // URL for browser access

    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.public-url}") String publicUrl,
            @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.publicUrl = publicUrl;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String folderPrefix = "imagensMinIO/";
            String fileName = folderPrefix + UUID.randomUUID() + "-" + file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // Use public URL for browser access
            return publicUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new IllegalStateException("Error uploading file to MinIO", e);
        }
    }
}
