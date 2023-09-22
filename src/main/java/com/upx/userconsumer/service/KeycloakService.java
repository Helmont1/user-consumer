package com.upx.userconsumer.service;

import java.util.Collections;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.upx.userconsumer.model.UserKeycloakDTO;

@Service
public class KeycloakService {
    private String token;

    public KeycloakService(Keycloak keycloak) {
        this.token = keycloak.tokenManager().getAccessToken().getToken();
    }

    public String registerUser(UserKeycloakDTO userKeycloak) {
        try {
            var keycloakUser = createUserRepresentation(userKeycloak);
            return registerUserOnKeycloak(keycloakUser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String registerUserOnKeycloak(UserRepresentation keycloakUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var keycloakUserString = objectMapper.writeValueAsString(keycloakUser);
            var json = objectMapper.readTree(keycloakUserString);   
            ((ObjectNode) json).remove("userProfileMetadata");
            HttpEntity<JsonNode> requestEntity = new HttpEntity<>(json, headers);
    
            var restTemplate = new RestTemplate();
            restTemplate.exchange(
                    "http://localhost:6065/admin/realms/upx-tickets/users",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class,
                    "upx-tickets");
    
            headers = new HttpHeaders();
            headers.setBearerAuth(token);
    
            var uri = UriComponentsBuilder.fromHttpUrl("http://localhost:6065/admin/realms/upx-tickets/users")
                    .queryParam("email", keycloakUser.getEmail())
                    .build()
                    .toUri();
    
            var request = new RequestEntity<Void>(headers, HttpMethod.GET, uri);
    
            ResponseEntity<UserRepresentation[]> responseEntity = restTemplate.exchange(
                    request,
                    UserRepresentation[].class);
            var userBody = responseEntity.getBody();
            if (userBody == null || userBody.length == 0) {
                throw new RuntimeException("User not found");
            }
            var user = userBody[0];
            return user.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UserRepresentation createUserRepresentation(UserKeycloakDTO user) {
        var keycloakUser = new UserRepresentation();
        keycloakUser.setEmail(user.getEmail());
        keycloakUser.setUsername(user.getEmail());
        keycloakUser.setEnabled(true);
        keycloakUser.setFirstName(user.getUserName());
        keycloakUser.setCredentials(
                Collections.singletonList(createCredentials(user)));

        return keycloakUser;
    }

    private CredentialRepresentation createCredentials(UserKeycloakDTO user) {
        var credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(true);
        credential.setValue(user.getEmail());
        return credential;
    }

}
