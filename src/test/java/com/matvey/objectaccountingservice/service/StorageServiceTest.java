package com.matvey.objectaccountingservice.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private StorageService storageService;

    private static final long MAX_PDF_SIZE = 52428800L;
    private static final long MAX_IMAGE_SIZE = 10485760L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "maxPdfSize", MAX_PDF_SIZE);
        ReflectionTestUtils.setField(storageService, "maxImageSize", MAX_IMAGE_SIZE);
        ReflectionTestUtils.setField(storageService, "contractsBucket", "contracts");
        ReflectionTestUtils.setField(storageService, "pprsBucket", "pprs");
        ReflectionTestUtils.setField(storageService, "objectsBucket", "objects");
    }

    @Test
    void ensureBucketsExist_CreatesMissingBuckets() throws Exception {
        lenient().when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false);

        storageService.ensureBucketsExist();

        verify(minioClient, times(3)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, times(3)).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void ensureBucketsExist_SkipsExistingBuckets() throws Exception {
        lenient().when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true);

        storageService.ensureBucketsExist();

        verify(minioClient, times(3)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void uploadContract_ValidPdf_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[1024]
        );

        String result = storageService.uploadContract(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".pdf"));
        verify(minioClient).putObject(any());
    }

    @Test
    void uploadContract_EmptyFile_ThrowsException() throws MinioException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> storageService.uploadContract(file));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    void uploadContract_WrongContentType_ThrowsException() throws MinioException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[1024]
        );

        assertThrows(IllegalArgumentException.class, () -> storageService.uploadContract(file));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    void uploadContract_TooLarge_ThrowsException() throws MinioException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[(int) MAX_PDF_SIZE + 1]
        );

        assertThrows(IllegalArgumentException.class, () -> storageService.uploadContract(file));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    void uploadObjectImage_ValidJpg_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[1024]
        );

        String result = storageService.uploadObjectImage(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".jpg"));
        verify(minioClient).putObject(any());
    }

    @Test
    void uploadObjectImage_ValidPng_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new byte[1024]
        );

        String result = storageService.uploadObjectImage(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".png"));
        verify(minioClient).putObject(any());
    }

    @Test
    void uploadObjectImage_EmptyFile_ThrowsException() throws MinioException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> storageService.uploadObjectImage(file));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    void uploadObjectImage_WrongContentType_ThrowsException() throws MinioException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[1024]
        );

        assertThrows(IllegalArgumentException.class, () -> storageService.uploadObjectImage(file));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    void uploadObjectImage_TooLarge_ThrowsException() throws MinioException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[(int) MAX_IMAGE_SIZE + 1]
        );

        assertThrows(IllegalArgumentException.class, () -> storageService.uploadObjectImage(file));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    void uploadPpr_ValidPdf_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[1024]
        );

        String result = storageService.uploadPpr(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".pdf"));
        verify(minioClient).putObject(any());
    }

    @Test
    void downloadContract_Success() throws Exception {
        String fileName = "test.pdf";
        byte[] fileData = "test content".getBytes();
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.readAllBytes()).thenReturn(fileData);

        when(minioClient.getObject(any())).thenReturn(response);

        byte[] result = storageService.downloadContract(fileName);

        assertNotNull(result);
        assertEquals(fileData.length, result.length);
        verify(minioClient).getObject(any());
    }

    @Test
    void downloadPpr_Success() throws Exception {
        String fileName = "test.pdf";
        byte[] fileData = "test content".getBytes();
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.readAllBytes()).thenReturn(fileData);

        when(minioClient.getObject(any())).thenReturn(response);

        byte[] result = storageService.downloadPpr(fileName);

        assertNotNull(result);
        assertEquals(fileData.length, result.length);
        verify(minioClient).getObject(any());
    }

    @Test
    void downloadObjectImage_Success() throws Exception {
        String fileName = "test.jpg";
        byte[] fileData = "test content".getBytes();
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.readAllBytes()).thenReturn(fileData);

        when(minioClient.getObject(any())).thenReturn(response);

        byte[] result = storageService.downloadObjectImage(fileName);

        assertNotNull(result);
        assertEquals(fileData.length, result.length);
        verify(minioClient).getObject(any());
    }

    @Test
    void deleteContract_Success() throws Exception {
        String fileName = "test.pdf";

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        storageService.deleteContract(fileName);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deletePpr_Success() throws Exception {
        String fileName = "test.pdf";

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        storageService.deletePpr(fileName);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteObjectImage_Success() throws Exception {
        String fileName = "test.jpg";

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        storageService.deleteObjectImage(fileName);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void replaceContract_Success() throws Exception {
        String oldFileName = "old.pdf";
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "new.pdf",
                "application/pdf",
                new byte[1024]
        );

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        storageService.replaceContract(oldFileName, newFile);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        verify(minioClient).putObject(any());
    }

    @Test
    void replacePpr_Success() throws Exception {
        String oldFileName = "old.pdf";
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "new.pdf",
                "application/pdf",
                new byte[1024]
        );

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        storageService.replacePpr(oldFileName, newFile);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        verify(minioClient).putObject(any());
    }

    @Test
    void replaceObjectImage_Success() throws Exception {
        String oldFileName = "old.jpg";
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "new.jpg",
                "image/jpeg",
                new byte[1024]
        );

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        storageService.replaceObjectImage(oldFileName, newFile);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        verify(minioClient).putObject(any());
    }
}
