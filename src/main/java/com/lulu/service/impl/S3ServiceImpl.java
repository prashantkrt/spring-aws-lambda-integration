package com.lulu.service.impl;

import com.lulu.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${app.s3.output-folder}")
    private String outputFolder;

    @Value("${app.s3.file-name}")
    private String fileName;

    @Override
    public boolean fileExists(String key) {

        log.info("Checking if file exists in S3. Bucket: {}, Key: {}", bucketName, key);

        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build());

            log.info("File exists in S3. Key: {}", key);
            return true;

        } catch (Exception e) {

            log.warn("File does not exist in S3. Key: {}", key);
            return false;
        }
    }

    @Override
    public void uploadCsv(String csvContent) {

        String key = String.format("%s/%s", outputFolder, fileName);

        log.info("Uploading CSV to S3. Bucket: {}, Key: {}", bucketName, key);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/csv")
                    .build();

            s3Client.putObject(request, RequestBody.fromString(csvContent));
            log.info("Successfully uploaded CSV to S3. Key: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload CSV to S3. Key: {}", key, e);
            throw e;
        }
    }

    @Override
    public List<S3Object> listFiles(String prefix) {

        log.info("Listing files from S3. Bucket: {}, Prefix: {}", bucketName, prefix);

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        List<S3Object> objects = s3Client.listObjectsV2(request).contents();
        log.info("Found {} files in S3 with prefix: {}", objects.size(), prefix);
        return objects;
    }


    @Override
    public String readFile(String key) {

        log.info("Reading file from S3. Bucket: {}, Key: {}", bucketName, key);

        try (InputStream is = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())) {

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Successfully read file from S3. Key: {}, Size: {} bytes",
                    key, content.length());
            return content;
        } catch (IOException e) {

            log.error("Error reading file from S3. Key: {}", key, e);
            throw new RuntimeException("Error reading S3 file", e);
        }
    }


}
