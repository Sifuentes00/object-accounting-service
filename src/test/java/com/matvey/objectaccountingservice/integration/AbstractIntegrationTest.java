package com.matvey.objectaccountingservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

    protected static PostgreSQLContainer<?> postgres;
    protected static MinIOContainer minio;
    protected static KeycloakContainer keycloak;

    protected static ObjectMapper objectMapper = new ObjectMapper();
    protected static RestTemplate restTemplate;

    @Autowired(required = false)
    protected MinioClient minioClient;

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));
        restTemplate = new RestTemplate(factory);

        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();

        minio = new MinIOContainer("minio/minio:latest")
                .withCommand("server /data --console-address :9001")
                .withExposedPorts(9000, 9001);
        minio.start();

        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:21.1.1")
                .withRealmImportFile("/test-realm.json")
                .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*Keycloak.*started.*", 1));
        keycloak.start();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String jwkSetUri = keycloak.getAuthServerUrl() + "/realms/test-realm/protocol/openid-connect/certs";
        int maxRetries = 10;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(jwkSetUri, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    break;
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                retryCount++;
            }
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("minio.endpoint", minio::getS3URL);
        registry.add("minio.access-key", minio::getUserName);
        registry.add("minio.secret-key", minio::getPassword);

        String issuerUri = keycloak.getAuthServerUrl() + "/realms/test-realm";
        String jwkSetUri = keycloak.getAuthServerUrl() + "/realms/test-realm/protocol/openid-connect/certs";

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> jwkSetUri);
    }

    protected static String getKeycloakUrl() {
        return keycloak.getAuthServerUrl();
    }

    protected static String getKeycloakRealm() {
        return "test-realm";
    }

    protected static String getKeycloakToken() {
        String tokenUrl = getKeycloakUrl() + "/realms/test-realm/protocol/openid-connect/token";
        String body = "grant_type=password&client_id=object-accounting-client&client_secret=test-secret&username=testadmin&password=adminpassword";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            HttpStatusCode statusCode = response.getStatusCode();

            if (statusCode.is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.get("access_token").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    protected static String getMinioEndpoint() {
        return minio.getS3URL();
    }

    protected static String getMinioAccessKey() {
        return minio.getUserName();
    }

    protected static String getMinioSecretKey() {
        return minio.getPassword();
    }
}
