package com.farmchainx.config;

import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Seeds the database with an initial admin account when none exists.
 *
 * Credentials are read from application.yml / environment variables so
 * nothing is hardcoded into compiled artifacts.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initAdminUser(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   AppProperties appProperties) {
        return args -> {
            AppProperties.Admin admin = appProperties.getAdmin();

            Optional<User> configuredAdmin = userRepository.findByEmail(admin.getEmail());
            if (configuredAdmin.isPresent()) {
                if (configuredAdmin.get().getRole() != UserRole.ADMIN) {
                    throw new IllegalStateException(
                            "Configured administrator account does not have the ADMIN role.");
                }
                log.info("Configured administrator account already exists – skipping seed.");
                return;
            }

            User adminUser = new User();
            adminUser.setFullName(admin.getFullName());
            adminUser.setEmail(admin.getEmail());
            adminUser.setPassword(passwordEncoder.encode(admin.getPassword()));
            adminUser.setRole(UserRole.ADMIN);
            adminUser.setPhoneNumber("0000000000");
            adminUser.setCreatedAt(LocalDateTime.now());

            userRepository.save(adminUser);
            log.info("Admin account created.");
        };
    }
}
