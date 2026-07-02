package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.dto.request.RegisterRequestDto;
import com.matvey.objectaccountingservice.dto.response.AuthResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthResponseDto register(RegisterRequestDto request) {
        String adminToken = getAdminToken();
        String userId = createUserInKeycloak(request, adminToken);
        
        try {
            ensureRoleExists(adminToken, "USER");
            assignRoleToUser(userId, adminToken, "USER");
        } catch (Exception e) {
            System.err.println("Failed to assign USER role: " + e.getMessage());
        }
        
        String token = loginUser(request.getUsername(), request.getPassword());
        String fullName = getUserFullNameFromToken(token);
        String email = getUserEmailFromToken(token);
        String role = getUserRoleFromToken(token);
        return new AuthResponseDto(token, fullName, email, role);
    }

    public AuthResponseDto login(String username, String password) {
        String token = loginUser(username, password);
        String fullName = getUserFullNameFromToken(token);
        String email = getUserEmailFromToken(token);
        String role = getUserRoleFromToken(token);
        return new AuthResponseDto(token, fullName, email, role);
    }

    private String getAdminToken() {
        String url = keycloakUrl + "/realms/master/protocol/openid-connect/token";
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", "admin-cli");
        params.add("username", "admin");
        params.add("password", "admin");
        params.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Не удалось получить токен администратора");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось разобрать ответ с токеном администратора", e);
        }
    }

    private void ensureRoleExists(String adminToken, String roleName) {
        try {
            String roleId = getRoleId(adminToken, roleName);
            System.out.println("Role " + roleName + " exists with ID: " + roleId);
        } catch (Exception e) {
            System.out.println("Role " + roleName + " not found, creating it...");
            createRole(adminToken, roleName);
        }
    }

    private void createRole(String adminToken, String roleName) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/roles";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        String roleJson = String.format(
            "{\"name\":\"%s\",\"description\":\"%s role\"}",
            roleName,
            roleName
        );

        HttpEntity<String> entity = new HttpEntity<>(roleJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.CONFLICT) {
            throw new RuntimeException("Не удалось создать роль: " + response.getBody());
        }
    }

    private String getRoleId(String adminToken, String roleName) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Не удалось получить роль: " + response.getBody());
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось разобрать ответ с ролью", e);
        }
    }

    private void assignRoleToUserById(String userId, String adminToken, String roleId) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        String roleJson = String.format(
            "[{\"id\":\"%s\",\"name\":\"USER\"}]",
            roleId
        );

        HttpEntity<String> entity = new HttpEntity<>(roleJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT && response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Не удалось назначить роль пользователю: " + response.getBody());
        }
    }

    private String createUserInKeycloak(RegisterRequestDto request, String adminToken) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        String userJson = String.format(
            "{\"username\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"enabled\":true,\"credentials\":[{\"type\":\"password\",\"value\":\"%s\",\"temporary\":false}]}",
            request.getUsername(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getPassword()
        );

        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Не удалось создать пользователя в Keycloak: " + response.getBody());
        }

        String location = response.getHeaders().getLocation().toString();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private void assignRoleToUser(String userId, String adminToken, String roleName) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        String roleId = getRoleId(adminToken, roleName);
        String roleJson = String.format(
            "[{\"id\":\"%s\",\"name\":\"%s\"}]",
            roleId,
            roleName
        );

        System.out.println("Assigning role " + roleName + " to user " + userId);
        System.out.println("URL: " + url);
        System.out.println("Role JSON: " + roleJson);

        HttpEntity<String> entity = new HttpEntity<>(roleJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        if (response.getStatusCode() != HttpStatus.NO_CONTENT && response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Не удалось назначить роль пользователю: " + response.getBody());
        }
    }

    private String getUserRoleFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            JsonNode root = objectMapper.readTree(payload);
            JsonNode roles = root.get("realm_access").get("roles");
            if (roles != null && roles.isArray()) {
                for (JsonNode role : roles) {
                    String roleName = role.asText();
                    if ("ADMIN".equals(roleName)) {
                        return "ADMIN";
                    }
                }
                if (roles.size() > 0) {
                    return roles.get(0).asText();
                }
            }
            return "USER";
        } catch (Exception e) {
            return "USER";
        }
    }

    private String getUserFullNameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            JsonNode root = objectMapper.readTree(payload);
            String firstName = root.get("given_name") != null ? root.get("given_name").asText() : "";
            String lastName = root.get("family_name") != null ? root.get("family_name").asText() : "";
            return (firstName + " " + lastName).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String getUserEmailFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            JsonNode root = objectMapper.readTree(payload);
            return root.get("email") != null ? root.get("email").asText() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String loginUser(String username, String password) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "password");
        params.add("username", username);
        params.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Не удалось войти пользователю: " + response.getBody());
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось разобрать ответ входа", e);
        }
    }
}
