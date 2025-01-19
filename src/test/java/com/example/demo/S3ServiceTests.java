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
import java.net.MalformedURLException;

import com.example.demo.service.S3Service;

public class S3ServiceTests {

    @Mock
    private AmazonS3 s3Client;

    @InjectMocks
    private S3Service s3Service;

    private MockMultipartFile file;
    private String bucketName = "test-bucket";
    private String objectKey = "test-video.mp4";
    private String contentType = "video/mp4";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        file = new MockMultipartFile("file", "test-video.mp4", contentType, "dummy content".getBytes());
    }

    @Test
    public void testUploadFile_Success() throws IOException {
        // Arrange
        when(s3Client.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());
        
        // Act
        String fileName = s3Service.uploadFile(file);
        
        // Assert
        assertNotNull(fileName);
        System.out.println("Generated file name: " + fileName);
        assertFalse(fileName.startsWith("test-video.mp4"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    public void testUploadFile_InvalidContentType() throws IOException {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "dummy content".getBytes());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            s3Service.uploadFile(invalidFile);
        });
    }

    @Test
    public void testGeneratePresignedUrl_Success() {
        // Arrange
        Duration expiration = Duration.ofMinutes(10);
        String expectedUrl = "http://mocked-url.com";
        URL mockUrl = null;
        try {
            mockUrl = new URL(expectedUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);
    
        // Act
        String url = s3Service.generatePresignedUrl(objectKey, expiration);
    
        // Assert
        assertNotNull(url);
        assertEquals(expectedUrl, url);
        verify(s3Client, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    public void testIsVideoContentType_Valid() {
        // Arrange
        String validContentType = "video/mp4";

        // Act
        boolean isVideo = s3Service.isVideoContentType(validContentType);

        // Assert
        assertTrue(isVideo);
    }

    @Test
    public void testIsVideoContentType_Invalid() {
        // Arrange
        String invalidContentType = "image/jpeg";

        // Act
        boolean isVideo = s3Service.isVideoContentType(invalidContentType);

        // Assert
        assertFalse(isVideo);
    }
}
