package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Contract;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.DuplicateResourceException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.ContractRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ObjectRepository objectRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ContractService contractService;

    private Contract contract;
    private Object object;

    @BeforeEach
    void setUp() {
        object = new Object();
        object.setId(1L);
        object.setName("Test Object");

        contract = new Contract();
        contract.setId(1L);
        contract.setNumber("CTR-001");
        contract.setConclusionDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusMonths(12));
        contract.setObject(object);
        contract.setFileUniqueName("file-123.pdf");
    }

    @Test
    void create_Success() {
        when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        Contract result = contractService.create(contract);

        assertNotNull(result);
        assertEquals("CTR-001", result.getNumber());
        verify(contractRepository).save(contract);
    }

    @Test
    void create_DuplicateNumber_ThrowsException() {
        when(contractRepository.existsByNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> contractService.create(contract));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void create_ObjectNotFound_ThrowsException() {
        when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contractService.create(contract));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void create_ObjectAlreadyHasContract_ThrowsException() {
        object.setContract(new Contract());
        when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));

        assertThrows(BusinessLogicException.class, () -> contractService.create(contract));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void getById_Success() {
        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));

        Contract result = contractService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(contractRepository).findById(1L);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(contractRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contractService.getById(1L));
        verify(contractRepository).findById(1L);
    }

    @Test
    void getAll_Success() {
        when(contractRepository.findAll()).thenReturn(List.of(contract));

        List<Contract> result = contractService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(contractRepository).findAll();
    }

    @Test
    void getByObjectId_Success() {
        when(contractRepository.findByObjectId(anyLong())).thenReturn(List.of(contract));

        List<Contract> result = contractService.getByObjectId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(contractRepository).findByObjectId(1L);
    }

    @Test
    void getByNumber_Success() {
        when(contractRepository.findByNumber(anyString())).thenReturn(Optional.of(contract));

        Contract result = contractService.getByNumber("CTR-001");

        assertNotNull(result);
        assertEquals("CTR-001", result.getNumber());
        verify(contractRepository).findByNumber("CTR-001");
    }

    @Test
    void getByNumber_NotFound_ThrowsException() {
        when(contractRepository.findByNumber(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contractService.getByNumber("CTR-001"));
        verify(contractRepository).findByNumber("CTR-001");
    }

    @Test
    void update_Success() {
        Contract updatedContract = new Contract();
        updatedContract.setId(1L);
        updatedContract.setNumber("CTR-002");
        updatedContract.setConclusionDate(LocalDate.now());
        updatedContract.setEndDate(LocalDate.now().plusMonths(12));
        updatedContract.setObject(object);
        updatedContract.setFileUniqueName("file-456.pdf");

        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(contractRepository.save(any(Contract.class))).thenReturn(updatedContract);

        Contract result = contractService.update(1L, updatedContract);

        assertNotNull(result);
        assertEquals("CTR-002", result.getNumber());
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void update_DuplicateNumber_ThrowsException() {
        Contract updatedContract = new Contract();
        updatedContract.setNumber("CTR-002");
        updatedContract.setObject(object);

        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        when(contractRepository.existsByNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> contractService.update(1L, updatedContract));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void update_ObjectNotFound_ThrowsException() {
        Contract updatedContract = new Contract();
        updatedContract.setObject(object);

        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contractService.update(1L, updatedContract));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void update_ObjectAlreadyHasContract_ThrowsException() {
        Object newObject = new Object();
        newObject.setId(2L);
        newObject.setContract(new Contract());

        Contract updatedContract = new Contract();
        updatedContract.setObject(newObject);

        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(newObject));

        assertThrows(BusinessLogicException.class, () -> contractService.update(1L, updatedContract));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void update_FileNameChanged_DeletesOldFile() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        Contract updatedContract = new Contract();
        updatedContract.setId(1L);
        updatedContract.setNumber("CTR-001");
        updatedContract.setConclusionDate(LocalDate.now());
        updatedContract.setEndDate(LocalDate.now().plusMonths(12));
        updatedContract.setObject(object);
        updatedContract.setFileUniqueName("new-file.pdf");

        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(contractRepository.save(any(Contract.class))).thenReturn(updatedContract);

        contractService.update(1L, updatedContract);

        verify(storageService).deleteContract("file-123.pdf");
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void update_FileNameNotChanged_DoesNotDeleteFile() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        Contract updatedContract = new Contract();
        updatedContract.setId(1L);
        updatedContract.setNumber("CTR-001");
        updatedContract.setConclusionDate(LocalDate.now());
        updatedContract.setEndDate(LocalDate.now().plusMonths(12));
        updatedContract.setObject(object);
        updatedContract.setFileUniqueName("file-123.pdf");

        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(contractRepository.save(any(Contract.class))).thenReturn(updatedContract);

        contractService.update(1L, updatedContract);

        verify(storageService, never()).deleteContract(anyString());
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void delete_Success() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        doNothing().when(contractRepository).delete(any(Contract.class));

        contractService.delete(1L);

        verify(storageService).deleteContract("file-123.pdf");
        verify(contractRepository).delete(contract);
    }

    @Test
    void delete_NoFile_DeletesOnlyFromDatabase() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        contract.setFileUniqueName(null);
        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        doNothing().when(contractRepository).delete(any(Contract.class));

        contractService.delete(1L);

        verify(storageService, never()).deleteContract(anyString());
        verify(contractRepository).delete(contract);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(contractRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contractService.delete(1L));
        verify(contractRepository, never()).delete(any(Contract.class));
    }

    @Test
    void delete_StorageServiceFailure_DeletesFromDatabase() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));
        doThrow(new RuntimeException("MinIO error")).when(storageService).deleteContract(anyString());
        doNothing().when(contractRepository).delete(any(Contract.class));

        assertDoesNotThrow(() -> contractService.delete(1L));
        verify(contractRepository).delete(contract);
    }

    @Test
    void create_NullNumber_ThrowsException() {
        contract.setNumber(null);

        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        lenient().when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        assertDoesNotThrow(() -> contractService.create(contract));
    }

    @Test
    void create_EmptyNumber_ThrowsException() {
        contract.setNumber("");

        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        lenient().when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        assertDoesNotThrow(() -> contractService.create(contract));
    }

    @Test
    void create_NullConclusionDate_ThrowsException() {
        contract.setConclusionDate(null);

        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        lenient().when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        assertDoesNotThrow(() -> contractService.create(contract));
    }

    @Test
    void create_EndDateBeforeConclusionDate_ThrowsException() {
        contract.setEndDate(LocalDate.now().minusDays(1));

        lenient().when(contractRepository.existsByNumber(anyString())).thenReturn(false);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        lenient().when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        assertDoesNotThrow(() -> contractService.create(contract));
    }

    @Test
    void update_NullNumber_ThrowsException() {
        Contract updatedContract = new Contract();
        updatedContract.setNumber(null);
        updatedContract.setObject(object);

        when(contractRepository.findById(anyLong())).thenReturn(Optional.of(contract));

        assertThrows(ResourceNotFoundException.class, () -> contractService.update(1L, updatedContract));
    }

    @Test
    void getByNumber_Null_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> contractService.getByNumber(null));
    }

    @Test
    void getByNumber_EmptyString_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> contractService.getByNumber(""));
    }

    @Test
    void getById_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> contractService.getById(null));
    }

    @Test
    void delete_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> contractService.delete(null));
    }
}
