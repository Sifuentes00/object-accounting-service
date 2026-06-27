package com.matvey.objectaccountingservice.integration;


import com.matvey.objectaccountingservice.dto.request.CustomerRequestDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
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
class CustomerControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @AfterEach
    void tearDown() {
        customerRepository.deleteAll();
    }

    @Test
    void getAll_WithAuthentication_Success() throws Exception {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setLegalAddress("Test Address");
        Customer savedCustomer = customerRepository.save(customer);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedCustomer.getId()))
                .andExpect(jsonPath("$[0].name").value("Test Customer"));
    }

    @Test
    void getById_WithAuthentication_Success() throws Exception {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setLegalAddress("Test Address");
        Customer savedCustomer = customerRepository.save(customer);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/v1/customers/" + savedCustomer.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCustomer.getId()))
                .andExpect(jsonPath("$.name").value("Test Customer"));
    }

    @Test
    void create_WithAdminRole_Success() throws Exception {
        CustomerRequestDto request = new CustomerRequestDto();
        request.setName("New Customer");
        request.setLegalAddress("New Address");

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Customer"));
    }

    @Test
    void update_WithAdminRole_Success() throws Exception {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setLegalAddress("Test Address");
        Customer savedCustomer = customerRepository.save(customer);

        CustomerRequestDto request = new CustomerRequestDto();
        request.setName("Updated Customer");
        request.setLegalAddress("Updated Address");

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(put("/api/v1/customers/" + savedCustomer.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Customer"));
    }

    @Test
    void delete_WithAdminRole_Success() throws Exception {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setLegalAddress("Test Address");
        Customer savedCustomer = customerRepository.save(customer);

        String token = getKeycloakToken();
        assertNotNull(token);

        mockMvc.perform(delete("/api/v1/customers/" + savedCustomer.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(customerRepository.existsById(savedCustomer.getId()));
    }

    @Test
    void getAll_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keycloakAndPostgres_Success() {
        assertNotNull(getKeycloakUrl());
        assertEquals("test-realm", getKeycloakRealm());

        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setLegalAddress("Test Address");
        Customer savedCustomer = customerRepository.save(customer);

        assertNotNull(savedCustomer);
        assertNotNull(savedCustomer.getId());
        assertTrue(customerRepository.existsById(savedCustomer.getId()));
    }
}
