package com.example.demo.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;

@Service
public class S3Service {
    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private final AmazonS3 s3Client;

    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Validate content type for videos
        String contentType = file.getContentType();
        if (contentType == null || !isVideoContentType(contentType)) {
            throw new IllegalArgumentException("Only video files are allowed");
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.getSize());

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.Private);

        s3Client.putObject(putObjectRequest);
        return fileName;
    }

    public boolean isVideoContentType(String contentType) {
        return Arrays.asList(
                "video/mp4",
                "video/mpeg",
                "video/quicktime",
                "video/x-msvideo",
                "video/webm"
        ).contains(contentType);
    }

    public String generatePresignedUrl(String objectKey, Duration expiration) {
        java.util.Date expiry = new java.util.Date();
        long expTimeMillis = expiry.getTime() + expiration.toMillis();
        expiry.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiry);

        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

}
