package com.matvey.objectaccountingservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matvey.objectaccountingservice.dto.request.ObjectRequestDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.enums.WorkType;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
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
class ObjectControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectRepository objectRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Customer testCustomer;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setName("Test Customer");
        testCustomer.setLegalAddress("Test Address");
        testCustomer = customerRepository.save(testCustomer);

        testEmployee = new Employee();
        testEmployee.setFullName("Test Employee");
        testEmployee.setPhoneNumber("+1234567890");
        testEmployee.setPosition("Engineer");
        testEmployee.setCustomer(testCustomer);
        testEmployee = employeeRepository.save(testEmployee);
    }

    @AfterEach
    void tearDown() {
        objectRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void getAll_WithAuthentication_Success() throws Exception {
        Object object = new Object();
        object.setName("Test Object");
        object.setAddress("Test Address");
        object.setStatus("IN_PROGRESS");
        object.setWorkType(WorkType.DESIGN);
        object.setCustomer(testCustomer);
        object.setResponsibleEmployee(testEmployee);
        Object savedObject = objectRepository.save(object);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/objects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedObject.getId()))
                .andExpect(jsonPath("$[0].name").value("Test Object"));
    }

    @Test
    void getById_WithAuthentication_Success() throws Exception {
        Object object = new Object();
        object.setName("Test Object");
        object.setAddress("Test Address");
        object.setStatus("IN_PROGRESS");
        object.setWorkType(WorkType.DESIGN);
        object.setCustomer(testCustomer);
        object.setResponsibleEmployee(testEmployee);
        Object savedObject = objectRepository.save(object);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/objects/" + savedObject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedObject.getId()))
                .andExpect(jsonPath("$.name").value("Test Object"));
    }

    @Test
    void create_WithAdminRole_Success() throws Exception {
        ObjectRequestDto request = new ObjectRequestDto();
        request.setName("New Object");
        request.setAddress("New Address");
        request.setStatus("PLANNING");
        request.setWorkType(WorkType.GEODESY);
        request.setCustomerId(testCustomer.getId());
        request.setResponsibleEmployeeId(testEmployee.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(post("/api/v1/objects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Object"));
    }

    @Test
    void update_WithAdminRole_Success() throws Exception {
        Object object = new Object();
        object.setName("Test Object");
        object.setAddress("Test Address");
        object.setStatus("IN_PROGRESS");
        object.setWorkType(WorkType.DESIGN);
        object.setCustomer(testCustomer);
        object.setResponsibleEmployee(testEmployee);
        Object savedObject = objectRepository.save(object);

        ObjectRequestDto request = new ObjectRequestDto();
        request.setName("Updated Object");
        request.setAddress("Updated Address");
        request.setStatus("COMPLETED");
        request.setWorkType(WorkType.CONSTRUCTION_INSTALLATION);
        request.setCustomerId(testCustomer.getId());
        request.setResponsibleEmployeeId(testEmployee.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(put("/api/v1/objects/" + savedObject.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Object"));
    }

    @Test
    void delete_WithAdminRole_Success() throws Exception {
        Object object = new Object();
        object.setName("Test Object");
        object.setAddress("Test Address");
        object.setStatus("IN_PROGRESS");
        object.setWorkType(WorkType.DESIGN);
        object.setCustomer(testCustomer);
        object.setResponsibleEmployee(testEmployee);
        Object savedObject = objectRepository.save(object);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(delete("/api/v1/objects/" + savedObject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(objectRepository.existsById(savedObject.getId()));
    }

    @Test
    void getAll_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/objects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keycloakAndPostgres_Success() {
        assertNotNull(getKeycloakUrl());
        assertEquals("test-realm", getKeycloakRealm());

        Object object = new Object();
        object.setName("Test Object");
        object.setAddress("Test Address");
        object.setStatus("IN_PROGRESS");
        object.setWorkType(WorkType.DESIGN);
        object.setCustomer(testCustomer);
        object.setResponsibleEmployee(testEmployee);
        Object savedObject = objectRepository.save(object);

        assertNotNull(savedObject);
        assertNotNull(savedObject.getId());
        assertTrue(objectRepository.existsById(savedObject.getId()));
    }
}
