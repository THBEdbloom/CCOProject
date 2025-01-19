package com.example.demo;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import com.example.demo.service.S3Service;

public class S3ServiceTests {

    @Mock
    private AmazonS3 s3Client;

    @InjectMocks
    private S3Service s3Service; // Deine Service-Klasse, die uploadFile und generatePresignedUrl enthält

    private MockMultipartFile file;
    private String bucketName = "test-bucket";
    private String objectKey = "test-video.mp4";
    private String contentType = "video/mp4";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);  // Für Mockito in JUnit 5
        file = new MockMultipartFile("file", "test-video.mp4", contentType, "dummy content".getBytes());
    }

    @Test
    public void testUploadFile_Success() throws IOException {
        // Arrange
        when(s3Client.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());

        // Act
        String fileName = s3Service.uploadFile(file);

        // Assert
        assertNotNull(fileName); // Es sollte ein Dateiname zurückgegeben werden
        assertTrue(fileName.startsWith(String.valueOf(System.currentTimeMillis()))); // Es sollte den Timestamp voranstellen
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class)); // Überprüfe, dass die S3 putObject-Methode einmal aufgerufen wurde
    }

    @Test
    public void testUploadFile_InvalidContentType() throws IOException {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "dummy content".getBytes());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            s3Service.uploadFile(invalidFile); // Sollte eine IllegalArgumentException werfen
        });
    }

    @Test
    public void testGeneratePresignedUrl_Success() {
        // Arrange
        Duration expiration = Duration.ofMinutes(10);
        URL mockUrl = mock(URL.class);
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);

        // Act
        String url = s3Service.generatePresignedUrl(objectKey, expiration);

        // Assert
        assertNotNull(url); // Es sollte eine URL zurückgegeben werden
        verify(s3Client, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class)); // Überprüfe, dass die generatePresignedUrl-Methode einmal aufgerufen wurde
    }

    @Test
    public void testIsVideoContentType_Valid() {
        // Arrange
        String validContentType = "video/mp4";

        // Act
        boolean isVideo = s3Service.isVideoContentType(validContentType);

        // Assert
        assertTrue(isVideo); // Sollte true zurückgeben
    }

    @Test
    public void testIsVideoContentType_Invalid() {
        // Arrange
        String invalidContentType = "image/jpeg";

        // Act
        boolean isVideo = s3Service.isVideoContentType(invalidContentType);

        // Assert
        assertFalse(isVideo); // Sollte false zurückgeben
    }
}
