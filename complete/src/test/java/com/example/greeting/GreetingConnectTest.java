package com.example.greeting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class GreetingConnectTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void greet_withJsonBody_returnsJsonResponse() {
        WebTestClient webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();

        webTestClient
            .post()
            .uri("/connect/greeting.v1.GreetingService/Greet")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"name\":\"World\"}")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Hello, World!");
    }
}
