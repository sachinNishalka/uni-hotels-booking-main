package lk.spence.uni_hotels_booking_main.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .requestFactory(() -> new BufferingClientHttpRequestFactory(
                        new SimpleClientHttpRequestFactory()))
                .interceptors(loggingInterceptor())
                .build();
    }

    @Bean
    public ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            // Add correlation ID to outgoing requests
            String correlationId = org.slf4j.MDC.get("correlationId");
            if (correlationId != null) {
                request.getHeaders().add("X-Correlation-Id", correlationId);
            }
            return execution.execute(request, body);
        };
    }
}