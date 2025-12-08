package tqs.sportslink.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String minioUrl;

    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.url}") String minioUrl,
            @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.minioUrl = minioUrl;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // Construct public URL since we set download policy to anonymous
            return minioUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }
}
