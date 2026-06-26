package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Ppr;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");

        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John Doe");
        employee.setPhoneNumber("+1234567890");
        employee.setPosition("Manager");
        employee.setCustomer(customer);
        employee.setPprs(new ArrayList<>());
    }

    @Test
    void create_Success() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findAll()).thenReturn(List.of());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.create(employee);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        verify(employeeRepository).save(employee);
    }

    @Test
    void create_CustomerNotFound_ThrowsException() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.create(employee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void create_DuplicatePhoneNumber_ThrowsException() {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(2L);
        existingEmployee.setCustomer(customer);
        existingEmployee.setPhoneNumber("+1234567890");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findAll()).thenReturn(List.of(existingEmployee));

        assertThrows(BusinessLogicException.class, () -> employeeService.create(employee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void create_DifferentCustomer_SamePhoneNumber_Success() {
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        
        Employee existingEmployee = new Employee();
        existingEmployee.setId(2L);
        existingEmployee.setCustomer(otherCustomer);
        existingEmployee.setPhoneNumber("+1234567890");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findAll()).thenReturn(List.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.create(employee);

        assertNotNull(result);
        verify(employeeRepository).save(employee);
    }

    @Test
    void getById_Success() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));

        Employee result = employeeService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository).findById(1L);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(1L));
        verify(employeeRepository).findById(1L);
    }

    @Test
    void getAll_Success() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository).findAll();
    }

    @Test
    void getByCustomerId_Success() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.getByCustomerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository).findAll();
    }

    @Test
    void update_Success() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setFullName("Jane Doe");
        updatedEmployee.setPhoneNumber("+9876543210");
        updatedEmployee.setPosition("Developer");
        updatedEmployee.setCustomer(customer);

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        Employee result = employeeService.update(1L, updatedEmployee);

        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void update_SamePhoneNumber_Success() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setFullName("Jane Doe");
        updatedEmployee.setPhoneNumber("+1234567890");
        updatedEmployee.setPosition("Developer");
        updatedEmployee.setCustomer(customer);

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        Employee result = employeeService.update(1L, updatedEmployee);

        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void update_CustomerNotFound_ThrowsException() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setCustomer(customer);

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.update(1L, updatedEmployee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void update_DuplicatePhoneNumber_ThrowsException() {
        Employee otherEmployee = new Employee();
        otherEmployee.setId(2L);
        otherEmployee.setCustomer(customer);
        otherEmployee.setPhoneNumber("+9876543210");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setFullName("Jane Doe");
        updatedEmployee.setPhoneNumber("+9876543210");
        updatedEmployee.setCustomer(customer);

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(employeeRepository.findAll()).thenReturn(List.of(employee, otherEmployee));

        assertThrows(BusinessLogicException.class, () -> employeeService.update(1L, updatedEmployee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void update_NotFound_ThrowsException() {
        Employee updatedEmployee = new Employee();

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.update(1L, updatedEmployee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void delete_Success() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(any(Employee.class));

        employeeService.delete(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(1L));
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void delete_WithPprs_ThrowsException() {
        employee.setPprs(List.of(new Ppr()));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));

        assertThrows(BusinessLogicException.class, () -> employeeService.delete(1L));
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void create_NullFullName_ThrowsException() {
        employee.setFullName(null);

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        lenient().when(employeeRepository.findAll()).thenReturn(List.of());
        lenient().when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> employeeService.create(employee));
    }

    @Test
    void create_EmptyFullName_ThrowsException() {
        employee.setFullName("");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        lenient().when(employeeRepository.findAll()).thenReturn(List.of());
        lenient().when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> employeeService.create(employee));
    }

    @Test
    void create_NullPhoneNumber_ThrowsException() {
        employee.setPhoneNumber(null);

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        lenient().when(employeeRepository.findAll()).thenReturn(List.of());
        lenient().when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> employeeService.create(employee));
    }

    @Test
    void create_EmptyPhoneNumber_ThrowsException() {
        employee.setPhoneNumber("");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        lenient().when(employeeRepository.findAll()).thenReturn(List.of());
        lenient().when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> employeeService.create(employee));
    }

    @Test
    void create_NullPosition_ThrowsException() {
        employee.setPosition(null);

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        lenient().when(employeeRepository.findAll()).thenReturn(List.of());
        lenient().when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> employeeService.create(employee));
    }

    @Test
    void create_EmptyPosition_ThrowsException() {
        employee.setPosition("");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        lenient().when(employeeRepository.findAll()).thenReturn(List.of());
        lenient().when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> employeeService.create(employee));
    }

    @Test
    void getById_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(null));
    }

    @Test
    void delete_NullId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(null));
    }
}
