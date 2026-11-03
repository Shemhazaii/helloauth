package com.shemhazaicraft.helloauth.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class ObjectStorageService {

    private final MinioClient storageClient;
    private final StorageProperties storageProperties;

    public ObjectStorageService(MinioClient storageClient, StorageProperties storageProperties) {
        this.storageClient = storageClient;
        this.storageProperties = storageProperties;
    }

    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {

        try {
            storageClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.bucketName())
                            .object(objectKey)
                            .stream(
                                    inputStream,
                                    size,
                                    -1
                            )
                            .contentType(contentType)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to upload file: " + objectKey,
                    e
            );
        }

    }

    public String generateDownloadUrl(String objectKey) {
        try {
            return storageClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(storageProperties.bucketName())
                            .object(objectKey)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate download URL: " + objectKey,
                    e
            );
        }
    }

    public void delete(String objectKey) {
        try {
            storageClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(storageProperties.bucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to delete object: " + objectKey,
                    e
            );
        }
    }


}
