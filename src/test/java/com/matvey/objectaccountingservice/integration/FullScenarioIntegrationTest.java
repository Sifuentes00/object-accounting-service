package com.matvey.objectaccountingservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matvey.objectaccountingservice.dto.request.*;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.enums.WorkType;
import com.matvey.objectaccountingservice.repository.*;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;


@SpringBootTest
@AutoConfigureMockMvc
class FullScenarioIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectRepository objectRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PprRepository pprRepository;

    @Autowired
    private MinioClient minioClient;

    @BeforeEach
    void setUp() throws Exception {
        createBucketIfNotExists("contracts");
        createBucketIfNotExists("pprs");
        createBucketIfNotExists("objects");
    }

    @AfterEach
    void tearDown() throws Exception {
        pprRepository.deleteAll();
        contractRepository.deleteAll();
        objectRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();

        removeBucket("contracts");
        removeBucket("pprs");
        removeBucket("objects");
    }

    @Test
    void fullWorkflow_AllTechnologies_Success() throws Exception {
        String keycloakToken = getKeycloakToken();
        assertNotNull(keycloakToken);

        CustomerRequestDto customerRequest = new CustomerRequestDto();
        customerRequest.setName("TechCorp");
        customerRequest.setLegalAddress("123 Tech Street");

        String customerResponse = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + keycloakToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long customerId = objectMapper.readTree(customerResponse).get("id").asLong();

        EmployeeRequestDto employeeRequest = new EmployeeRequestDto();
        employeeRequest.setFullName("Jane Doe");
        employeeRequest.setPhoneNumber("+9876543210");
        employeeRequest.setPosition("Project Manager");
        employeeRequest.setCustomerId(customerId);

        String employeeResponse = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + keycloakToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long employeeId = objectMapper.readTree(employeeResponse).get("id").asLong();

        ObjectRequestDto objectRequest = new ObjectRequestDto();
        objectRequest.setName("Office Building A");
        objectRequest.setAddress("456 Construction Road");
        objectRequest.setStatus("IN_PROGRESS");
        objectRequest.setWorkType(WorkType.CONSTRUCTION_INSTALLATION);
        objectRequest.setCustomerId(customerId);
        objectRequest.setResponsibleEmployeeId(employeeId);

        String objectResponse = mockMvc.perform(post("/api/v1/objects")
                        .header("Authorization", "Bearer " + keycloakToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(objectRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long objectId = objectMapper.readTree(objectResponse).get("id").asLong();

        ContractRequestDto contractRequest = new ContractRequestDto();
        contractRequest.setNumber("CTR-2024-001");
        contractRequest.setConclusionDate(LocalDate.now());
        contractRequest.setEndDate(LocalDate.now().plusMonths(24));
        contractRequest.setFileUniqueName("file-ctr-001.pdf");
        contractRequest.setObjectId(objectId);

        mockMvc.perform(post("/api/v1/contracts")
                        .header("Authorization", "Bearer " + keycloakToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractRequest)))
                .andExpect(status().isCreated());

        PprRequestDto pprRequest = new PprRequestDto();
        pprRequest.setNumber("PPR-2024-001");
        pprRequest.setName("Project Planning Document");
        pprRequest.setArchiveNumber("ARCH-2024-001");
        pprRequest.setFileUniqueName("file-ppr-001.pdf");
        pprRequest.setObjectId(objectId);
        pprRequest.setEmployeeId(employeeId);

        mockMvc.perform(post("/api/v1/pprs")
                        .header("Authorization", "Bearer " + keycloakToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pprRequest)))
                .andExpect(status().isCreated());

        minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                        .bucket("contracts")
                        .object("test-file.pdf")
                        .stream(
                                new ByteArrayInputStream("Test content".getBytes()),
                                (long) "Test content".getBytes().length,
                                -1L
                        )                        .contentType("application/pdf")
                        .build()
        );

        assertTrue(minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder().bucket("contracts").build()
        ));

        assertNotNull(getKeycloakUrl());
        assertEquals("test-realm", getKeycloakRealm());

        Customer savedCustomer = customerRepository.findById(customerId).orElse(null);
        assertNotNull(savedCustomer);
        assertEquals("TechCorp", savedCustomer.getName());
    }

    @Test
    void authentication_WithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorization_UserCannotCreateCustomer() throws Exception {
        CustomerRequestDto request = new CustomerRequestDto();
        request.setName("Test Customer");
        request.setLegalAddress("Test Address");

        mockMvc.perform(post("/api/v1/customers")
                        .with(httpBasic("testuser", "testpassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }

    private void removeBucket(String bucketName) throws Exception {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (exists) {
                var objects = minioClient.listObjects(
                        io.minio.ListObjectsArgs.builder().bucket(bucketName).build()
                );
                for (var object : objects) {
                    minioClient.removeObject(
                            io.minio.RemoveObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(object.get().objectName())
                                    .build()
                    );
                }
                minioClient.removeBucket(
                        io.minio.RemoveBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove bucket: " + bucketName, e);
        }
    }
}
