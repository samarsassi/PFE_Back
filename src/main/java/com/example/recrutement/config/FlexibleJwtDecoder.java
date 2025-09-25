package com.example.recrutement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FlexibleJwtDecoder {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String primaryIssuerUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Create a JWT decoder that can handle the external Keycloak URL
        RestTemplate restTemplate = new RestTemplate();
        
        // Use the external Keycloak URL for JWK validation
        // This will validate tokens issued by the external Keycloak instance
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(primaryIssuerUri + "/protocol/openid-connect/certs")
                .restOperations(restTemplate)
                .build();
        
        return jwtDecoder;
    }
}
