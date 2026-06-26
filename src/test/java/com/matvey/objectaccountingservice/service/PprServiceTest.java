package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.entity.Ppr;
import com.matvey.objectaccountingservice.exception.DuplicateResourceException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import com.matvey.objectaccountingservice.repository.PprRepository;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PprServiceTest {

    @Mock
    private PprRepository pprRepository;

    @Mock
    private ObjectRepository objectRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private PprService pprService;

    private Ppr ppr;
    private Object object;
    private Employee employee;

    @BeforeEach
    void setUp() {
        object = new Object();
        object.setId(1L);
        object.setName("Test Object");

        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John Doe");

        ppr = new Ppr();
        ppr.setId(1L);
        ppr.setNumber("PPR-001");
        ppr.setName("Test PPR");
        ppr.setArchiveNumber("ARCH-001");
        ppr.setObject(object);
        ppr.setEmployee(employee);
        ppr.setFileUniqueName("ppr-123.pdf");
    }

    @Test
    void create_Success() {
        when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(pprRepository.save(any(Ppr.class))).thenReturn(ppr);

        Ppr result = pprService.create(ppr);

        assertNotNull(result);
        assertEquals("PPR-001", result.getNumber());
        verify(pprRepository).save(ppr);
    }

    @Test
    void create_DuplicateNumber_ThrowsException() {
        when(pprRepository.existsByNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> pprService.create(ppr));
        verify(pprRepository, never()).save(any(Ppr.class));
    }

    @Test
    void create_ObjectNotFound_ThrowsException() {
        when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pprService.create(ppr));
        verify(pprRepository, never()).save(any(Ppr.class));
    }

    @Test
    void create_WithEmployee_Success() {
        when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(pprRepository.save(any(Ppr.class))).thenReturn(ppr);

        Ppr result = pprService.create(ppr);

        assertNotNull(result);
        assertEquals(employee, result.getEmployee());
        verify(pprRepository).save(ppr);
    }

    @Test
    void create_EmployeeNotFound_ThrowsException() {
        when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pprService.create(ppr));
        verify(pprRepository, never()).save(any(Ppr.class));
    }

    @Test
    void getById_Success() {
        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));

        Ppr result = pprService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(pprRepository).findById(1L);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(pprRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pprService.getById(1L));
        verify(pprRepository).findById(1L);
    }

    @Test
    void getAll_Success() {
        when(pprRepository.findAll()).thenReturn(List.of(ppr));

        List<Ppr> result = pprService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pprRepository).findAll();
    }

    @Test
    void getByObjectId_Success() {
        when(pprRepository.findByObjectId(anyLong())).thenReturn(List.of(ppr));

        List<Ppr> result = pprService.getByObjectId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pprRepository).findByObjectId(1L);
    }

    @Test
    void getByNumber_Success() {
        when(pprRepository.findByNumber(anyString())).thenReturn(Optional.of(ppr));

        Ppr result = pprService.getByNumber("PPR-001");

        assertNotNull(result);
        assertEquals("PPR-001", result.getNumber());
        verify(pprRepository).findByNumber("PPR-001");
    }

    @Test
    void getByNumber_NotFound_ThrowsException() {
        when(pprRepository.findByNumber(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pprService.getByNumber("PPR-001"));
        verify(pprRepository).findByNumber("PPR-001");
    }

    @Test
    void getByArchiveNumber_Success() {
        when(pprRepository.findByArchiveNumber(anyString())).thenReturn(List.of(ppr));

        List<Ppr> result = pprService.getByArchiveNumber("ARCH-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pprRepository).findByArchiveNumber("ARCH-001");
    }

    @Test
    void update_Success() {
        Ppr updatedPpr = new Ppr();
        updatedPpr.setNumber("PPR-002");
        updatedPpr.setName("Updated PPR");
        updatedPpr.setArchiveNumber("ARCH-002");
        updatedPpr.setObject(object);
        updatedPpr.setEmployee(employee);
        updatedPpr.setFileUniqueName("new-ppr.pdf");

        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(pprRepository.save(any(Ppr.class))).thenReturn(updatedPpr);

        Ppr result = pprService.update(1L, updatedPpr);

        assertNotNull(result);
        assertEquals("PPR-002", result.getNumber());
        verify(pprRepository).save(any(Ppr.class));
    }

    @Test
    void update_DuplicateNumber_ThrowsException() {
        Ppr updatedPpr = new Ppr();
        updatedPpr.setNumber("PPR-002");
        updatedPpr.setObject(object);

        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        when(pprRepository.existsByNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> pprService.update(1L, updatedPpr));
        verify(pprRepository, never()).save(any(Ppr.class));
    }

    @Test
    void update_ObjectNotFound_ThrowsException() {
        Ppr updatedPpr = new Ppr();
        updatedPpr.setObject(object);

        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pprService.update(1L, updatedPpr));
        verify(pprRepository, never()).save(any(Ppr.class));
    }

    @Test
    void update_EmployeeNotFound_ThrowsException() {
        Ppr updatedPpr = new Ppr();
        updatedPpr.setObject(object);
        updatedPpr.setEmployee(employee);

        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pprService.update(1L, updatedPpr));
        verify(pprRepository, never()).save(any(Ppr.class));
    }

    @Test
    void update_RemoveEmployee_Success() {
        Ppr updatedPpr = new Ppr();
        updatedPpr.setNumber("PPR-001");
        updatedPpr.setName("Test PPR");
        updatedPpr.setArchiveNumber("ARCH-001");
        updatedPpr.setObject(object);
        updatedPpr.setEmployee(null);

        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(pprRepository.save(any(Ppr.class))).thenReturn(updatedPpr);

        Ppr result = pprService.update(1L, updatedPpr);

        assertNotNull(result);
        assertNull(result.getEmployee());
        verify(pprRepository).save(any(Ppr.class));
    }

    @Test
    void update_FileNameChanged_DeletesOldFile() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        Ppr updatedPpr = new Ppr();
        updatedPpr.setNumber("PPR-001");
        updatedPpr.setName("Test PPR");
        updatedPpr.setArchiveNumber("ARCH-001");
        updatedPpr.setObject(object);
        updatedPpr.setFileUniqueName("new-ppr.pdf");

        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(pprRepository.save(any(Ppr.class))).thenReturn(updatedPpr);

        pprService.update(1L, updatedPpr);

        verify(storageService).deletePpr("ppr-123.pdf");
        verify(pprRepository).save(any(Ppr.class));
    }

    @Test
    void update_FileNameNotChanged_DoesNotDeleteFile() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        Ppr updatedPpr = new Ppr();
        updatedPpr.setNumber("PPR-001");
        updatedPpr.setName("Test PPR");
        updatedPpr.setArchiveNumber("ARCH-001");
        updatedPpr.setObject(object);
        updatedPpr.setFileUniqueName("ppr-123.pdf");

        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(pprRepository.save(any(Ppr.class))).thenReturn(updatedPpr);

        pprService.update(1L, updatedPpr);

        verify(storageService, never()).deletePpr(anyString());
        verify(pprRepository).save(any(Ppr.class));
    }

    @Test
    void delete_Success() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        doNothing().when(pprRepository).delete(any(Ppr.class));

        pprService.delete(1L);

        verify(storageService).deletePpr("ppr-123.pdf");
        verify(pprRepository).delete(ppr);
    }

    @Test
    void delete_NoFile_DeletesOnlyFromDatabase() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        ppr.setFileUniqueName(null);
        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        doNothing().when(pprRepository).delete(any(Ppr.class));

        pprService.delete(1L);

        verify(storageService, never()).deletePpr(anyString());
        verify(pprRepository).delete(ppr);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(pprRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pprService.delete(1L));
        verify(pprRepository, never()).delete(any(Ppr.class));
    }

    @Test
    void delete_StorageServiceFailure_DeletesFromDatabase() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(pprRepository.findById(anyLong())).thenReturn(Optional.of(ppr));
        doThrow(new RuntimeException("MinIO error")).when(storageService).deletePpr(anyString());
        doNothing().when(pprRepository).delete(any(Ppr.class));

        assertDoesNotThrow(() -> pprService.delete(1L));
        verify(pprRepository).delete(ppr);
    }

    @Test
    void create_NullNumber_ThrowsException() {
        ppr.setNumber(null);

        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        lenient().when(pprRepository.save(any(Ppr.class))).thenReturn(ppr);

        assertDoesNotThrow(() -> pprService.create(ppr));
    }

    @Test
    void create_EmptyNumber_ThrowsException() {
        ppr.setNumber("");

        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        lenient().when(pprRepository.save(any(Ppr.class))).thenReturn(ppr);

        assertDoesNotThrow(() -> pprService.create(ppr));
    }

    @Test
    void create_NullName_ThrowsException() {
        ppr.setName(null);

        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        lenient().when(pprRepository.save(any(Ppr.class))).thenReturn(ppr);

        assertDoesNotThrow(() -> pprService.create(ppr));
    }

    @Test
    void create_EmptyName_ThrowsException() {
        ppr.setName("");

        lenient().when(pprRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        lenient().when(pprRepository.save(any(Ppr.class))).thenReturn(ppr);

        assertDoesNotThrow(() -> pprService.create(ppr));
    }

    @Test
    void getByNumber_Null_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> pprService.getByNumber(null));
    }

    @Test
    void getByNumber_EmptyString_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> pprService.getByNumber(""));
    }

    @Test
    void getById_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> pprService.getById(null));
    }

    @Test
    void delete_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> pprService.delete(null));
    }
}
