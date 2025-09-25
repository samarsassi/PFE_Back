package com.example.recrutement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ContextIT {

    @Test
    void contextLoads() {
        // Boots the Spring context to verify wiring against the test profile
    }
}


