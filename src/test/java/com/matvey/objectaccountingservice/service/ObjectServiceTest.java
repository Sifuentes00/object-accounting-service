package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Contract;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.entity.Ppr;
import com.matvey.objectaccountingservice.enums.WorkType;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObjectServiceTest {

    @Mock
    private ObjectRepository objectRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ObjectService objectService;

    private Object object;
    private Customer customer;
    private Employee employee;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");

        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John Doe");

        object = new Object();
        object.setId(1L);
        object.setName("Test Object");
        object.setAddress("Test Address");
        object.setStatus("IN_PROGRESS");
        object.setWorkType(WorkType.CONSTRUCTION_INSTALLATION);
        object.setCustomer(customer);
        object.setResponsibleEmployee(employee);
        object.setImageUniqueName("image-123.jpg");
        object.setPprs(new ArrayList<>());
    }

    @Test
    void create_Success() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(objectRepository.save(any(Object.class))).thenReturn(object);

        Object result = objectService.create(object);

        assertNotNull(result);
        assertEquals("Test Object", result.getName());
        verify(objectRepository).save(object);
    }

    @Test
    void create_CustomerNotFound_ThrowsException() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
        verify(objectRepository, never()).save(any(Object.class));
    }

    @Test
    void create_WithEmployee_Success() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(objectRepository.save(any(Object.class))).thenReturn(object);

        Object result = objectService.create(object);

        assertNotNull(result);
        assertEquals(employee, result.getResponsibleEmployee());
        verify(objectRepository).save(object);
    }

    @Test
    void create_EmployeeNotFound_ThrowsException() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
        verify(objectRepository, never()).save(any(Object.class));
    }

    @Test
    void getById_Success() {
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));

        Object result = objectService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(objectRepository).findById(1L);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(objectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> objectService.getById(1L));
        verify(objectRepository).findById(1L);
    }

    @Test
    void getAll_Success() {
        when(objectRepository.findAll()).thenReturn(List.of(object));

        List<Object> result = objectService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(objectRepository).findAll();
    }

    @Test
    void getByCustomerId_Success() {
        when(objectRepository.findByCustomerId(anyLong())).thenReturn(List.of(object));

        List<Object> result = objectService.getByCustomerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(objectRepository).findByCustomerId(1L);
    }

    @Test
    void getByStatus_Success() {
        when(objectRepository.findByStatus(anyString())).thenReturn(List.of(object));

        List<Object> result = objectService.getByStatus("IN_PROGRESS");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(objectRepository).findByStatus("IN_PROGRESS");
    }

    @Test
    void getByWorkType_Success() {
        when(objectRepository.findByWorkType(any(WorkType.class))).thenReturn(List.of(object));

        List<Object> result = objectService.getByWorkType(WorkType.CONSTRUCTION_INSTALLATION);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(objectRepository).findByWorkType(WorkType.CONSTRUCTION_INSTALLATION);
    }

    @Test
    void update_Success() {
        Object updatedObject = new Object();
        updatedObject.setName("Updated Object");
        updatedObject.setAddress("Updated Address");
        updatedObject.setStatus("COMPLETED");
        updatedObject.setWorkType(WorkType.DESIGN);
        updatedObject.setCustomer(customer);
        updatedObject.setResponsibleEmployee(employee);
        updatedObject.setImageUniqueName("new-image.jpg");

        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(objectRepository.save(any(Object.class))).thenReturn(updatedObject);

        Object result = objectService.update(1L, updatedObject);

        assertNotNull(result);
        assertEquals("Updated Object", result.getName());
        verify(objectRepository).save(any(Object.class));
    }

    @Test
    void update_CustomerNotFound_ThrowsException() {
        Object updatedObject = new Object();
        updatedObject.setCustomer(customer);

        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> objectService.update(1L, updatedObject));
        verify(objectRepository, never()).save(any(Object.class));
    }

    @Test
    void update_EmployeeNotFound_ThrowsException() {
        Object updatedObject = new Object();
        updatedObject.setCustomer(customer);
        updatedObject.setResponsibleEmployee(employee);

        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> objectService.update(1L, updatedObject));
        verify(objectRepository, never()).save(any(Object.class));
    }

    @Test
    void update_RemoveEmployee_Success() {
        Object updatedObject = new Object();
        updatedObject.setName("Test Object");
        updatedObject.setAddress("Test Address");
        updatedObject.setStatus("IN_PROGRESS");
        updatedObject.setWorkType(WorkType.CONSTRUCTION_INSTALLATION);
        updatedObject.setCustomer(customer);
        updatedObject.setResponsibleEmployee(null);

        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(objectRepository.save(any(Object.class))).thenReturn(updatedObject);

        Object result = objectService.update(1L, updatedObject);

        assertNotNull(result);
        assertNull(result.getResponsibleEmployee());
        verify(objectRepository).save(any(Object.class));
    }

    @Test
    void update_ImageNameChanged_DeletesOldImage() throws Exception {
        Object updatedObject = new Object();
        updatedObject.setName("Test Object");
        updatedObject.setAddress("Test Address");
        updatedObject.setStatus("IN_PROGRESS");
        updatedObject.setWorkType(WorkType.CONSTRUCTION_INSTALLATION);
        updatedObject.setCustomer(customer);
        updatedObject.setImageUniqueName("new-image.jpg");

        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(objectRepository.save(any(Object.class))).thenReturn(updatedObject);

        objectService.update(1L, updatedObject);

        verify(storageService).deleteObjectImage("image-123.jpg");
        verify(objectRepository).save(any(Object.class));
    }

    @Test
    void update_ImageNameNotChanged_DoesNotDeleteImage() throws Exception {
        Object updatedObject = new Object();
        updatedObject.setName("Test Object");
        updatedObject.setAddress("Test Address");
        updatedObject.setStatus("IN_PROGRESS");
        updatedObject.setWorkType(WorkType.CONSTRUCTION_INSTALLATION);
        updatedObject.setCustomer(customer);
        updatedObject.setImageUniqueName("image-123.jpg");

        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(objectRepository.save(any(Object.class))).thenReturn(updatedObject);

        objectService.update(1L, updatedObject);

        verify(storageService, never()).deleteObjectImage(anyString());
        verify(objectRepository).save(any(Object.class));
    }

    @Test
    void delete_Success() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        doNothing().when(objectRepository).delete(any(Object.class));

        objectService.delete(1L);

        verify(storageService).deleteObjectImage("image-123.jpg");
        verify(objectRepository).delete(object);
    }

    @Test
    void delete_NoImage_DeletesOnlyFromDatabase() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        object.setImageUniqueName(null);
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        doNothing().when(objectRepository).delete(any(Object.class));

        objectService.delete(1L);

        verify(storageService, never()).deleteObjectImage(anyString());
        verify(objectRepository).delete(object);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(objectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> objectService.delete(1L));
        verify(objectRepository, never()).delete(any(Object.class));
    }

    @Test
    void delete_WithContract_ThrowsException() {
        object.setContract(new Contract());
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));

        assertThrows(BusinessLogicException.class, () -> objectService.delete(1L));
        verify(objectRepository, never()).delete(any(Object.class));
    }

    @Test
    void delete_WithPprs_ThrowsException() {
        object.setPprs(List.of(new Ppr()));
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));

        assertThrows(BusinessLogicException.class, () -> objectService.delete(1L));
        verify(objectRepository, never()).delete(any(Object.class));
    }

    @Test
    void delete_StorageServiceFailure_DeletesFromDatabase() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(objectRepository.findById(anyLong())).thenReturn(Optional.of(object));
        doThrow(new RuntimeException("MinIO error")).when(storageService).deleteObjectImage(anyString());
        doNothing().when(objectRepository).delete(any(Object.class));

        assertDoesNotThrow(() -> objectService.delete(1L));
        verify(objectRepository).delete(object);
    }

    @Test
    void create_NullName_ThrowsException() {
        object.setName(null);

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
    }

    @Test
    void create_EmptyName_ThrowsException() {
        object.setName("");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
    }

    @Test
    void create_NullAddress_ThrowsException() {
        object.setAddress(null);

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
    }

    @Test
    void create_EmptyAddress_ThrowsException() {
        object.setAddress("");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
    }

    @Test
    void create_NullStatus_ThrowsException() {
        object.setStatus(null);

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
    }

    @Test
    void create_EmptyStatus_ThrowsException() {
        object.setStatus("");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
    }

    @Test
    void create_NullWorkType_ThrowsException() {
        object.setWorkType(null);

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> objectService.create(object));
    }

    @Test
    void getById_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> objectService.getById(null));
    }

    @Test
    void delete_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> objectService.delete(null));
    }

    @Test
    void getByStatus_Null_ThrowsException() {
        assertDoesNotThrow(() -> objectService.getByStatus(null));
    }

    @Test
    void getByStatus_EmptyString_ThrowsException() {
        assertDoesNotThrow(() -> objectService.getByStatus(""));
    }
}
