package com.interviewgene.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Service for handling file storage operations with S3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final S3Client s3Client;

    @Value("${resume.file.storage.s3.bucket}")
    private String bucketName;

    @Value("${resume.file.storage.s3.prefix:resumes/}")
    private String keyPrefix;

    /**
     * Upload file to S3 and return the key
     */
    public String uploadFile(MultipartFile file, UUID candidateId, UUID resumeId) {
        try {
            String key = generateS3Key(candidateId, resumeId, file.getOriginalFilename());
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(java.util.Map.of(
                            "candidate-id", candidateId.toString(),
                            "resume-id", resumeId.toString(),
                            "original-filename", file.getOriginalFilename()
                    ))
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            log.info("Successfully uploaded file to S3: bucket={}, key={}", bucketName, key);
            return key;
            
        } catch (IOException e) {
            log.error("Failed to upload file to S3: candidateId={}, resumeId={}", candidateId, resumeId, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Download file from S3
     */
    public InputStream downloadFile(String s3Key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            return s3Client.getObject(getRequest);
            
        } catch (Exception e) {
            log.error("Failed to download file from S3: key={}", s3Key, e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    /**
     * Delete file from S3
     */
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted file from S3: key={}", s3Key);
            
        } catch (Exception e) {
            log.error("Failed to delete file from S3: key={}", s3Key, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    /**
     * Check if file exists in S3
     */
    public boolean fileExists(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence in S3: key={}", s3Key, e);
            return false;
        }
    }

    /**
     * Get file metadata from S3
     */
    public GetObjectResponse getFileMetadata(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            
            return GetObjectResponse.builder()
                    .contentLength(response.contentLength())
                    .contentType(response.contentType())
                    .lastModified(response.lastModified())
                    .metadata(response.metadata())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to get file metadata from S3: key={}", s3Key, e);
            throw new RuntimeException("Failed to get file metadata from S3", e);
        }
    }

    /**
     * Generate S3 key for resume file
     */
    private String generateS3Key(UUID candidateId, UUID resumeId, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return String.format("%soriginal/%s/%s.%s", keyPrefix, candidateId, resumeId, extension);
    }

    /**
     * Generate S3 key for processed resume data
     */
    public String generateProcessedDataKey(UUID candidateId, UUID resumeId) {
        return String.format("%sprocessed/%s/%s.json", keyPrefix, candidateId, resumeId);
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "bin";
    }

    /**
     * Upload processed resume data as JSON
     */
    public String uploadProcessedData(String jsonData, UUID candidateId, UUID resumeId) {
        try {
            String key = generateProcessedDataKey(candidateId, resumeId);
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .metadata(java.util.Map.of(
                            "candidate-id", candidateId.toString(),
                            "resume-id", resumeId.toString(),
                            "data-type", "processed-resume"
                    ))
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(jsonData));
            
            log.info("Successfully uploaded processed data to S3: bucket={}, key={}", bucketName, key);
            return key;
            
        } catch (Exception e) {
            log.error("Failed to upload processed data to S3: candidateId={}, resumeId={}", candidateId, resumeId, e);
            throw new RuntimeException("Failed to upload processed data to S3", e);
        }
    }
}