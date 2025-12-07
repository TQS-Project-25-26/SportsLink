package tqs.sportslink.B_Tests_unit;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import tqs.sportslink.service.MinioStorageService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitMinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioStorageService minioStorageService;

    private final String MINIO_URL = "http://localhost:9000";
    private final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        minioStorageService = new MinioStorageService(minioClient, MINIO_URL, BUCKET_NAME);
    }

    @Test
    void uploadFile_ShouldReturnCorrectUrl_WhenUploadSuccessful() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test data".getBytes());

        // When
        String resultUrl = minioStorageService.uploadFile(file);

        // Then
        assertNotNull(resultUrl);
        assertTrue(resultUrl.startsWith(MINIO_URL + "/" + BUCKET_NAME + "/"));
        assertTrue(resultUrl.endsWith("-test-image.jpg"));

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_ShouldThrowException_WhenMinioErrorOccurs() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test data".getBytes());

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> minioStorageService.uploadFile(file));

        assertEquals("Error uploading file to MinIO", exception.getMessage());
    }
}
