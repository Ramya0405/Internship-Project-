package com.farmchainx.persistence;

import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@EnabledIfSystemProperty(named = "farmchainx.mysql.integration", matches = "true")
class MySqlPersistenceIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void configureMySql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MySqlPersistenceIntegrationTest::jdbcUrl);
        registry.add("spring.datasource.username", () -> requiredEnvironmentVariable("DB_USERNAME"));
        registry.add("spring.datasource.password", () -> requiredEnvironmentVariable("DB_PASSWORD"));
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Test
    void connectsToMySqlAndPersistsThenReadsBackAUser() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertThat(metadata.getDatabaseProductName()).isEqualTo("MySQL");
            assertThat(connection.isValid(3)).isTrue();
        }

        String email = "mysql-integration-" + UUID.randomUUID() + "@example.test";
        User user = new User();
        user.setFullName("MySQL Integration User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("IntegrationOnlyPass123"));
        user.setRole(UserRole.CONSUMER);
        userRepository.saveAndFlush(user);

        userRepository.flush();
        userRepository.findByEmail(email).orElseThrow();
        assertThat(userRepository.findByEmail(email))
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.CONSUMER);
    }

    private static String jdbcUrl() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String database = System.getenv().getOrDefault("DB_NAME", "farmchainx");
        String sslMode = System.getenv().getOrDefault("DB_SSL_MODE", "DISABLED");
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?createDatabaseIfNotExist=false&sslMode=" + sslMode
                + "&serverTimezone=UTC";
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing: " + name);
        }
        return value;
    }
}
