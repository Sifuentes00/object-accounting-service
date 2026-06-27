package com.matvey.objectaccountingservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matvey.objectaccountingservice.dto.request.PprRequestDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.entity.Ppr;
import com.matvey.objectaccountingservice.enums.WorkType;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import com.matvey.objectaccountingservice.repository.PprRepository;
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
class PprControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PprRepository pprRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectRepository objectRepository;

    private Customer testCustomer;
    private Employee testEmployee;
    private Object testObject;

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

        testObject = new Object();
        testObject.setName("Test Object");
        testObject.setAddress("Test Address");
        testObject.setStatus("IN_PROGRESS");
        testObject.setWorkType(WorkType.DESIGN);
        testObject.setCustomer(testCustomer);
        testObject = objectRepository.save(testObject);
    }

    @AfterEach
    void tearDown() {
        pprRepository.deleteAll();
        objectRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void getAll_WithAuthentication_Success() throws Exception {
        Ppr ppr = new Ppr();
        ppr.setNumber("PPR-001");
        ppr.setName("Test PPR");
        ppr.setArchiveNumber("ARCH-001");
        ppr.setFileUniqueName("file-123.pdf");
        ppr.setObject(testObject);
        ppr.setEmployee(testEmployee);
        Ppr savedPpr = pprRepository.save(ppr);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/pprs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedPpr.getId()))
                .andExpect(jsonPath("$[0].number").value("PPR-001"));
    }

    @Test
    void getById_WithAuthentication_Success() throws Exception {
        Ppr ppr = new Ppr();
        ppr.setNumber("PPR-001");
        ppr.setName("Test PPR");
        ppr.setArchiveNumber("ARCH-001");
        ppr.setFileUniqueName("file-123.pdf");
        ppr.setObject(testObject);
        ppr.setEmployee(testEmployee);
        Ppr savedPpr = pprRepository.save(ppr);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/pprs/" + savedPpr.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPpr.getId()))
                .andExpect(jsonPath("$.number").value("PPR-001"));
    }

    @Test
    void create_WithAdminRole_Success() throws Exception {
        PprRequestDto request = new PprRequestDto();
        request.setNumber("PPR-NEW-001");
        request.setName("New PPR");
        request.setArchiveNumber("ARCH-NEW-001");
        request.setFileUniqueName("file-new.pdf");
        request.setObjectId(testObject.getId());
        request.setEmployeeId(testEmployee.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(post("/api/v1/pprs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("PPR-NEW-001"));
    }

    @Test
    void update_WithAdminRole_Success() throws Exception {
        Ppr ppr = new Ppr();
        ppr.setNumber("PPR-001");
        ppr.setName("Test PPR");
        ppr.setArchiveNumber("ARCH-001");
        ppr.setFileUniqueName("file-123.pdf");
        ppr.setObject(testObject);
        ppr.setEmployee(testEmployee);
        Ppr savedPpr = pprRepository.save(ppr);

        PprRequestDto request = new PprRequestDto();
        request.setNumber("PPR-UPDATED-001");
        request.setName("Updated PPR");
        request.setArchiveNumber("ARCH-UPDATED-001");
        request.setFileUniqueName("file-updated.pdf");
        request.setObjectId(testObject.getId());
        request.setEmployeeId(testEmployee.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(put("/api/v1/pprs/" + savedPpr.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("PPR-UPDATED-001"));
    }

    @Test
    void delete_WithAdminRole_Success() throws Exception {
        Ppr ppr = new Ppr();
        ppr.setNumber("PPR-001");
        ppr.setName("Test PPR");
        ppr.setArchiveNumber("ARCH-001");
        ppr.setFileUniqueName("file-123.pdf");
        ppr.setObject(testObject);
        ppr.setEmployee(testEmployee);
        Ppr savedPpr = pprRepository.save(ppr);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(delete("/api/v1/pprs/" + savedPpr.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(pprRepository.existsById(savedPpr.getId()));
    }

    @Test
    void getAll_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/pprs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keycloakAndPostgres_Success() {
        assertNotNull(getKeycloakUrl());
        assertEquals("test-realm", getKeycloakRealm());

        Ppr ppr = new Ppr();
        ppr.setNumber("PPR-001");
        ppr.setName("Test PPR");
        ppr.setArchiveNumber("ARCH-001");
        ppr.setFileUniqueName("file-123.pdf");
        ppr.setObject(testObject);
        ppr.setEmployee(testEmployee);
        Ppr savedPpr = pprRepository.save(ppr);

        assertNotNull(savedPpr);
        assertNotNull(savedPpr.getId());
        assertTrue(pprRepository.existsById(savedPpr.getId()));
    }
}
