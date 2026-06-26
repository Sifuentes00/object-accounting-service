package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.DuplicateResourceException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setLegalAddress("Test Address");
        customer.setObjects(new ArrayList<>());
        customer.setEmployees(new ArrayList<>());
    }

    @Test
    void create_Success() {
        when(customerRepository.existsByName(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.create(customer);

        assertNotNull(result);
        assertEquals("Test Customer", result.getName());
        verify(customerRepository).save(customer);
    }

    @Test
    void create_DuplicateName_ThrowsException() {
        when(customerRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.create(customer));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getById_Success() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        Customer result = customerService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository).findById(1L);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getById(1L));
        verify(customerRepository).findById(1L);
    }

    @Test
    void getAll_Success() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(customerRepository).findAll();
    }

    @Test
    void update_Success() {
        Customer updatedCustomer = new Customer();
        updatedCustomer.setName("Updated Customer");
        updatedCustomer.setLegalAddress("Updated Address");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(customerRepository.existsByName(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        Customer result = customerService.update(1L, updatedCustomer);

        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void update_SameName_Success() {
        Customer updatedCustomer = new Customer();
        updatedCustomer.setName("Test Customer");
        updatedCustomer.setLegalAddress("Updated Address");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        Customer result = customerService.update(1L, updatedCustomer);

        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void update_DuplicateName_ThrowsException() {
        Customer updatedCustomer = new Customer();
        updatedCustomer.setName("New Customer");
        updatedCustomer.setLegalAddress("Test Address");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(customerRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.update(1L, updatedCustomer));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void update_NotFound_ThrowsException() {
        Customer updatedCustomer = new Customer();

        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.update(1L, updatedCustomer));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void delete_Success() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).delete(any(Customer.class));

        customerService.delete(1L);

        verify(customerRepository).delete(customer);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.delete(1L));
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void delete_WithObjects_ThrowsException() {
        customer.setObjects(List.of(new Object()));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(BusinessLogicException.class, () -> customerService.delete(1L));
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void delete_WithEmployees_ThrowsException() {
        customer.setEmployees(List.of(new Employee()));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(BusinessLogicException.class, () -> customerService.delete(1L));
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void create_NullName_ThrowsException() {
        customer.setName(null);

        lenient().when(customerRepository.existsByName(anyString())).thenReturn(false);
        lenient().when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        assertDoesNotThrow(() -> customerService.create(customer));
    }

    @Test
    void create_EmptyName_ThrowsException() {
        customer.setName("");

        lenient().when(customerRepository.existsByName(anyString())).thenReturn(false);
        lenient().when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        assertDoesNotThrow(() -> customerService.create(customer));
    }

    @Test
    void create_NullLegalAddress_ThrowsException() {
        customer.setLegalAddress(null);

        lenient().when(customerRepository.existsByName(anyString())).thenReturn(false);
        lenient().when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        assertDoesNotThrow(() -> customerService.create(customer));
    }

    @Test
    void create_EmptyLegalAddress_ThrowsException() {
        customer.setLegalAddress("");

        lenient().when(customerRepository.existsByName(anyString())).thenReturn(false);
        lenient().when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        assertDoesNotThrow(() -> customerService.create(customer));
    }

    @Test
    void getById_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> customerService.getById(null));
    }

    @Test
    void delete_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> customerService.delete(null));
    }
}
