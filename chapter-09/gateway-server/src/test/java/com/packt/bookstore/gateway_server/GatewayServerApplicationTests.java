package com.packt.bookstore.gateway_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
class GatewayServerApplicationTests {

	@TestConfiguration
	static class SecurityTestConfig {
		@Bean
		ReactiveJwtDecoder reactiveJwtDecoder() {
			return mock(ReactiveJwtDecoder.class);
		}
	}

	@Test
	void contextLoads() {
	}

}
