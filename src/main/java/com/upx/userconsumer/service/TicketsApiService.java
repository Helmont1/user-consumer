package com.upx.userconsumer.service;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.upx.userconsumer.model.LoginModel;

@Service
public class TicketsApiService {
    private final RestTemplate restTemplate;
    private final Environment env;

    public TicketsApiService(RestTemplate restTemplate, Environment env) {
        this.restTemplate = restTemplate;
        this.env = env;
    }

    public String login() {
        var response = restTemplate.postForEntity(env.getProperty("ticketsapi.url.login"), new LoginModel(
                env.getProperty("ticketsapi.login.username"),
                env.getProperty("ticketsapi.login.password")), JsonNode.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error logging in");
        }
        var body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Error logging in");
        }

        return body.get("data").get("access_token").asText();
    }

    public void postKeycloakId(String keycloakId) {
        var headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + login());
        var response = restTemplate.postForEntity(env.getProperty("ticketsapi.url.keycloak"), keycloakId, Void.class);
        if (response.getStatusCode() != HttpStatus.CREATED ) {
            throw new RuntimeException("Error posting keycloak id");
        }
    }

}
