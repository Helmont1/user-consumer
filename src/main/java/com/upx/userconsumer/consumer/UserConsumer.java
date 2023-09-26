package com.upx.userconsumer.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.upx.userconsumer.model.UserKeycloakDTO;
import com.upx.userconsumer.service.KeycloakService;
import com.upx.userconsumer.service.TicketsApiService;

@Component
public class UserConsumer {
    private final KeycloakService keycloakService;
    private final TicketsApiService ticketsApiService;

    public UserConsumer(final KeycloakService keycloakService,
            final TicketsApiService ticketsApiService) {
        this.keycloakService = keycloakService;
        this.ticketsApiService = ticketsApiService;
    }

    @RabbitListener(queues = { "user-keycloak-queue" }, autoStartup = "true", concurrency = "3")
    public void consume(UserKeycloakDTO userKeycloak) {
        try {
            var keycloakId = keycloakService.registerUser(userKeycloak);
            ticketsApiService.postKeycloakId(keycloakId);
        } catch (Exception ignored) {
            // ignored
        }
    }
}
