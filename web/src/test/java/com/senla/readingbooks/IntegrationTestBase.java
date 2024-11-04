package com.senla.readingbooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mockMvc;

    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.6");

    static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4"))
            .withExposedPorts(6379);

    static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("elasticsearch:7.17.24");

    static final MinIOContainer minioContainer = new MinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2024-10-02T17-50-41Z"))
            .withExposedPorts(9000);

    @BeforeAll
    static void startContainers() {
        postgresContainer.start();
        redisContainer.start();
        elasticsearchContainer.start();
        minioContainer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);

        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);

        registry.add("minio.url", () -> "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        registry.add("minio.username", minioContainer::getUserName);
        registry.add("minio.password", minioContainer::getPassword);
    }
}