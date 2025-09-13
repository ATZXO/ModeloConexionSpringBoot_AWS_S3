package com.atz.cloud.aws_s3.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface S3Service {
    String createBucket(String bucketName);
    String bucketExists(String bucketName);
    List<String> listBuckets();
    Boolean uploadFile(String bucketName, String key, Path filePath);
    void downloadFile(String bucketName, String key) throws IOException;
    String presignedUploadUrl(String bucketName, String key, Duration duration);
    String presigedDownloadUrl(String bucketName, String key, Duration duration);
}
