package com.matvey.objectaccountingservice.integration;

import com.matvey.objectaccountingservice.dto.request.ContractRequestDto;
import com.matvey.objectaccountingservice.entity.Contract;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.enums.WorkType;
import com.matvey.objectaccountingservice.repository.ContractRepository;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContractControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectRepository objectRepository;

    private Customer testCustomer;
    private Object testObject;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setName("Test Customer");
        testCustomer.setLegalAddress("Test Address");
        testCustomer = customerRepository.save(testCustomer);

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
        contractRepository.deleteAll();
        objectRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void getAll_WithAuthentication_Success() throws Exception {
        Contract contract = new Contract();
        contract.setNumber("CTR-001");
        contract.setConclusionDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusMonths(12));
        contract.setFileUniqueName("file-123.pdf");
        contract.setObject(testObject);
        Contract savedContract = contractRepository.save(contract);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/contracts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedContract.getId()))
                .andExpect(jsonPath("$[0].number").value("CTR-001"));
    }

    @Test
    void getById_WithAuthentication_Success() throws Exception {
        Contract contract = new Contract();
        contract.setNumber("CTR-001");
        contract.setConclusionDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusMonths(12));
        contract.setFileUniqueName("file-123.pdf");
        contract.setObject(testObject);
        Contract savedContract = contractRepository.save(contract);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/contracts/" + savedContract.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedContract.getId()))
                .andExpect(jsonPath("$.number").value("CTR-001"));
    }

    @Test
    void create_WithAdminRole_Success() throws Exception {
        ContractRequestDto request = new ContractRequestDto();
        request.setNumber("CTR-NEW-001");
        request.setConclusionDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(12));
        request.setFileUniqueName("file-new.pdf");
        request.setObjectId(testObject.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(post("/api/v1/contracts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("CTR-NEW-001"));
    }

    @Test
    void update_WithAdminRole_Success() throws Exception {
        Contract contract = new Contract();
        contract.setNumber("CTR-001");
        contract.setConclusionDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusMonths(12));
        contract.setFileUniqueName("file-123.pdf");
        contract.setObject(testObject);
        Contract savedContract = contractRepository.save(contract);

        ContractRequestDto request = new ContractRequestDto();
        request.setNumber("CTR-UPDATED-001");
        request.setConclusionDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(12));
        request.setFileUniqueName("file-updated.pdf");
        request.setObjectId(testObject.getId());

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(put("/api/v1/contracts/" + savedContract.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("CTR-UPDATED-001"));
    }

    @Test
    void delete_WithAdminRole_Success() throws Exception {
        Contract contract = new Contract();
        contract.setNumber("CTR-001");
        contract.setConclusionDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusMonths(12));
        contract.setFileUniqueName("file-123.pdf");
        contract.setObject(testObject);
        Contract savedContract = contractRepository.save(contract);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(delete("/api/v1/contracts/" + savedContract.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(contractRepository.existsById(savedContract.getId()));
    }

    @Test
    void getAll_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/contracts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keycloakAndPostgres_Success() {
        assertNotNull(getKeycloakUrl());
        assertEquals("test-realm", getKeycloakRealm());

        Contract contract = new Contract();
        contract.setNumber("CTR-001");
        contract.setConclusionDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusMonths(12));
        contract.setFileUniqueName("file-123.pdf");
        contract.setObject(testObject);
        Contract savedContract = contractRepository.save(contract);

        assertNotNull(savedContract);
        assertNotNull(savedContract.getId());
        assertTrue(contractRepository.existsById(savedContract.getId()));
    }
}
