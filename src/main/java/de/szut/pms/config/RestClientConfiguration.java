package de.szut.pms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Stellt einen RestClient.Builder bereit, mit dem wir andere Services
 * (z. B. den Employee-Service) per HTTP aufrufen.
 */
@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
