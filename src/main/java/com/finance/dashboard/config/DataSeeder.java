package com.finance.dashboard.config;

import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ✅ Create or Update Admin (NO DELETE)
        User admin = userRepository.findByUsername("admin")
                .map(existingUser -> {
                    existingUser.setPassword(passwordEncoder.encode("admin123"));
                    existingUser.setEmail("admin@finance.com");
                    existingUser.setRole(Role.ADMIN);
                    existingUser.setActive(true);
                    return existingUser;
                })
                .orElseGet(() -> User.builder()
                        .username("admin")
                        .email("admin@finance.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .active(true)
                        .build());

        userRepository.save(admin);
        log.info("Admin user ready — username: admin");

        // ✅ Analyst
        if (!userRepository.existsByUsername("analyst")) {
            User analyst = User.builder()
                    .username("analyst")
                    .email("analyst@finance.com")
                    .password(passwordEncoder.encode("analyst123"))
                    .role(Role.ANALYST)
                    .active(true)
                    .build();
            userRepository.save(analyst);
            log.info("Analyst user created");
        }

        // ✅ Viewer
        if (!userRepository.existsByUsername("viewer")) {
            User viewer = User.builder()
                    .username("viewer")
                    .email("viewer@finance.com")
                    .password(passwordEncoder.encode("viewer123"))
                    .role(Role.VIEWER)
                    .active(true)
                    .build();
            userRepository.save(viewer);
            log.info("Viewer user created");
        }
    }
}
