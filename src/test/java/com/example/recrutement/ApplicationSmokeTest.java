package com.example.recrutement;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Disabled to avoid starting Spring context (DB) during unit tests")
@SpringBootTest
class ApplicationSmokeTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring ApplicationContext starts successfully
    }
}


