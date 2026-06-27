package com.matvey.objectaccountingservice.integration;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StorageServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MinioClient minioClient;

    @Test
    void minioConnection_Success() throws Exception {
        assertNotNull(minioClient);
        assertNotNull(getMinioEndpoint());
        assertNotNull(getMinioAccessKey());
        assertNotNull(getMinioSecretKey());
    }

    @Test
    void uploadAndDownloadFile_Success() throws Exception {
        String bucketName = "contracts";
        String objectName = "test-file.pdf";
        String content = "Test content";

        createBucketIfNotExists(bucketName);

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(
                                new ByteArrayInputStream(content.getBytes()),
                                (long) content.getBytes().length,
                                -1L
                        )
                        .contentType("application/pdf")
                        .build()
        );

        var response = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );

        assertNotNull(response);
        byte[] downloadedBytes = response.readAllBytes();
        assertEquals(content, new String(downloadedBytes));

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    @Test
    void deleteFile_Success() throws Exception {
        String bucketName = "contracts";
        String objectName = "test-file-delete.pdf";
        String content = "Test file content";

        createBucketIfNotExists(bucketName);

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(
                                new ByteArrayInputStream("Test content".getBytes()),
                                (long) "Test content".getBytes().length,
                                -1L
                        )
                        .contentType("application/pdf")
                        .build()
        );

        assertTrue(objectExists(bucketName, objectName));

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );

        assertFalse(objectExists(bucketName, objectName));
    }

    @Test
    void listObjects_Success() throws Exception {
        String bucketName = "contracts";
        String content = "Test content";

        createBucketIfNotExists(bucketName);

        // Clean up existing objects
        var existingObjects = minioClient.listObjects(
                io.minio.ListObjectsArgs.builder().bucket(bucketName).build()
        );
        for (var object : existingObjects) {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(object.get().objectName())
                            .build()
            );
        }

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object("file1.pdf")
                        .stream(
                                new ByteArrayInputStream("Test content".getBytes()),
                                (long) "Test content".getBytes().length,
                                -1L
                        )                        .contentType("application/pdf")
                        .build()
        );

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object("file2.pdf")
                        .stream(
                                new ByteArrayInputStream("Test content".getBytes()),
                                (long) "Test content".getBytes().length,
                                -1L
                        )                        .contentType("application/pdf")
                        .build()
        );

        var objects = minioClient.listObjects(
                io.minio.ListObjectsArgs.builder().bucket(bucketName).build()
        );

        int objectCount = 0;
        for (var object : objects) {
            objectCount++;
        }
        assertEquals(2, objectCount);

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object("file1.pdf")
                        .build()
        );

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object("file2.pdf")
                        .build()
        );
    }

    @Test
    void copyObjectBetweenBuckets_Success() throws Exception {
        String sourceBucket = "contracts";
        String destinationBucket = "pprs";
        String objectName = "test-file-copy.pdf";
        String content = "Test file content";

        createBucketIfNotExists(sourceBucket);
        createBucketIfNotExists(destinationBucket);

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket(sourceBucket)
                        .object(objectName)
                        .stream(
                                new ByteArrayInputStream("Test content".getBytes()),
                                (long) "Test content".getBytes().length,
                                -1L
                        )
                        .contentType("application/pdf")
                        .build()
        );

        var sourceResponse = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(sourceBucket)
                        .object(objectName)
                        .build()
        );
        byte[] sourceBytes = sourceResponse.readAllBytes();

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket(destinationBucket)
                        .object(objectName)
                        .stream(
                                new ByteArrayInputStream("Test content".getBytes()),
                                (long) "Test content".getBytes().length,
                                -1L
                        )                        .contentType("application/pdf")
                        .build()
        );

        assertTrue(objectExists(destinationBucket, objectName));

        var sourceResponse2 = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(sourceBucket)
                        .object(objectName)
                        .build()
        );
        byte[] sourceBytes2 = sourceResponse2.readAllBytes();

        var destResponse = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(destinationBucket)
                        .object(objectName)
                        .build()
        );
        byte[] destBytes = destResponse.readAllBytes();

        assertEquals(new String(sourceBytes2), new String(destBytes));

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(sourceBucket)
                        .object(objectName)
                        .build()
        );

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(destinationBucket)
                        .object(objectName)
                        .build()
        );
    }

    @Test
    void getObjectMetadata_Success() throws Exception {
        String bucketName = "contracts";
        String objectName = "test-file-metadata.pdf";
        String content = "Test content";

        createBucketIfNotExists(bucketName);

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(
                                new ByteArrayInputStream(content.getBytes()),
                                (long) content.getBytes().length,
                                -1L
                        )
                        .contentType("application/pdf")
                        .build()
        );

        var stat = minioClient.statObject(
                io.minio.StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );

        assertNotNull(stat);
        assertEquals(objectName, stat.object());
        assertEquals(content.getBytes().length, stat.size());

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }

    private boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    io.minio.StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}