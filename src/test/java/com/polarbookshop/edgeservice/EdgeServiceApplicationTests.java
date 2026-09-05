package com.polarbookshop.edgeservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.ReactiveRedisSessionRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class EdgeServiceApplicationTests {

    private static final int REDIS_PORT = 6379;

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.0"))
                    .withExposedPorts(REDIS_PORT);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(REDIS_PORT)
        );
    }

    @Autowired
    ReactiveSessionRepository<? extends Session> sessionRepository;

    @Test
    void verifyThatSpringContextLoads() {
        assertThat(sessionRepository)
                .isInstanceOf(ReactiveRedisSessionRepository.class);
    }
}