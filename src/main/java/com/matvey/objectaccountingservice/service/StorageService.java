package com.matvey.objectaccountingservice.service;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.contracts}")
    private String contractsBucket;

    @Value("${minio.bucket.pprs}")
    private String pprsBucket;

    @Value("${minio.bucket.objects}")
    private String objectsBucket;

    @Value("${minio.file.max-size.pdf}")
    private long maxPdfSize;

    @Value("${minio.file.max-size.image}")
    private long maxImageSize;

    private static final List<String> PDF_CONTENT_TYPES = Arrays.asList("application/pdf");
    private static final List<String> IMAGE_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png");

    public void ensureBucketsExist() {
        try {
            ensureBucketExists(contractsBucket);
            ensureBucketExists(pprsBucket);
            ensureBucketExists(objectsBucket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure buckets exist", e);
        }
    }

    private void ensureBucketExists(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    public String uploadContract(MultipartFile file) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        validatePdfFile(file);
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        uploadFile(contractsBucket, fileName, file.getInputStream(), file.getSize(), file.getContentType());
        return fileName;
    }

    public String uploadPpr(MultipartFile file) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        validatePdfFile(file);
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        uploadFile(pprsBucket, fileName, file.getInputStream(), file.getSize(), file.getContentType());
        return fileName;
    }

    public String uploadObjectImage(MultipartFile file) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        validateImageFile(file);
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        uploadFile(objectsBucket, fileName, file.getInputStream(), file.getSize(), file.getContentType());
        return fileName;
    }

    private void validatePdfFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (!PDF_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        if (file.getSize() > maxPdfSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (maxPdfSize / 1024 / 1024) + "MB");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (!IMAGE_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPG and PNG files are allowed");
        }
        if (file.getSize() > maxImageSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (maxImageSize / 1024 / 1024) + "MB");
        }
    }

    private void uploadFile(String bucketName, String fileName, InputStream inputStream, long size, String contentType) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, size, -1L)
                        .contentType(contentType)
                        .build()
        );
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    public byte[] downloadContract(String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return downloadFile(contractsBucket, fileName);
    }

    public byte[] downloadPpr(String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return downloadFile(pprsBucket, fileName);
    }

    public byte[] downloadObjectImage(String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return downloadFile(objectsBucket, fileName);
    }

    private byte[] downloadFile(String bucketName, String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
        return stream.readAllBytes();
    }

    public void deleteContract(String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        deleteFile(contractsBucket, fileName);
    }

    public void deletePpr(String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        deleteFile(pprsBucket, fileName);
    }

    public void deleteObjectImage(String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        deleteFile(objectsBucket, fileName);
    }

    private void deleteFile(String bucketName, String fileName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    public void replaceContract(String oldFileName, MultipartFile newFile) throws Exception {
        deleteContract(oldFileName);
        uploadContract(newFile);
    }

    public void replacePpr(String oldFileName, MultipartFile newFile) throws Exception {
        deletePpr(oldFileName);
        uploadPpr(newFile);
    }

    public void replaceObjectImage(String oldFileName, MultipartFile newFile) throws Exception {
        deleteObjectImage(oldFileName);
        uploadObjectImage(newFile);
    }
}
