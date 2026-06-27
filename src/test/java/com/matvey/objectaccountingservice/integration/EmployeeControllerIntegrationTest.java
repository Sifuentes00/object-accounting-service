package com.matvey.objectaccountingservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matvey.objectaccountingservice.dto.request.EmployeeRequestDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setName("Test Customer");
        testCustomer.setLegalAddress("Test Address");
        testCustomer = customerRepository.save(testCustomer);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void getAll_WithAuthentication_Success() throws Exception {
        Employee employee = new Employee();
        employee.setFullName("Test Employee");
        employee.setPhoneNumber("+1234567890");
        employee.setPosition("Engineer");
        employee.setCustomer(testCustomer);
        Employee savedEmployee = employeeRepository.save(employee);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedEmployee.getId()))
                .andExpect(jsonPath("$[0].fullName").value("Test Employee"));
    }

    @Test
    void getById_WithAuthentication_Success() throws Exception {
        Employee employee = new Employee();
        employee.setFullName("Test Employee");
        employee.setPhoneNumber("+1234567890");
        employee.setPosition("Engineer");
        employee.setCustomer(testCustomer);
        Employee savedEmployee = employeeRepository.save(employee);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/employees/" + savedEmployee.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedEmployee.getId()))
                .andExpect(jsonPath("$.fullName").value("Test Employee"));
    }

    @Test
    void create_WithAdminRole_Success() throws Exception {
        EmployeeRequestDto request = new EmployeeRequestDto();
        request.setFullName("New Employee");
        request.setPhoneNumber("+9876543210");
        request.setPosition("Manager");
        request.setCustomerId(testCustomer.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("New Employee"));
    }

    @Test
    void update_WithAdminRole_Success() throws Exception {
        Employee employee = new Employee();
        employee.setFullName("Test Employee");
        employee.setPhoneNumber("+1234567890");
        employee.setPosition("Engineer");
        employee.setCustomer(testCustomer);
        Employee savedEmployee = employeeRepository.save(employee);

        EmployeeRequestDto request = new EmployeeRequestDto();
        request.setFullName("Updated Employee");
        request.setPhoneNumber("+9876543210");
        request.setPosition("Manager");
        request.setCustomerId(testCustomer.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(put("/api/v1/employees/" + savedEmployee.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Employee"));
    }

    @Test
    void delete_WithAdminRole_Success() throws Exception {
        Employee employee = new Employee();
        employee.setFullName("Test Employee");
        employee.setPhoneNumber("+1234567890");
        employee.setPosition("Engineer");
        employee.setCustomer(testCustomer);
        Employee savedEmployee = employeeRepository.save(employee);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(delete("/api/v1/employees/" + savedEmployee.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(employeeRepository.existsById(savedEmployee.getId()));
    }

    @Test
    void getAll_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keycloakAndPostgres_Success() {
        assertNotNull(getKeycloakUrl());
        assertEquals("test-realm", getKeycloakRealm());

        Employee employee = new Employee();
        employee.setFullName("Test Employee");
        employee.setPhoneNumber("+1234567890");
        employee.setPosition("Engineer");
        employee.setCustomer(testCustomer);
        Employee savedEmployee = employeeRepository.save(employee);

        assertNotNull(savedEmployee);
        assertNotNull(savedEmployee.getId());
        assertTrue(employeeRepository.existsById(savedEmployee.getId()));
    }
}
