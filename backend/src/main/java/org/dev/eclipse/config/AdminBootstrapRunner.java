package org.dev.eclipse.config;

import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder,
            BootstrapAdminProperties bootstrapAdminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminProperties = bootstrapAdminProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapAdminProperties.isEnabled()) {
            logger.info("Bootstrap admin creation is disabled.");
            return;
        }
        if (!hasRequiredFields()) {
            logger.warn("Bootstrap admin configuration is incomplete. Admin seed skipped.");
            return;
        }
        userRepository.findByEmail(bootstrapAdminProperties.getEmail()).ifPresentOrElse(existingUser -> logger.info("Bootstrap admin already exists for email: {}", existingUser.getEmail()), this::createBootstrapAdmin);
    }

    private boolean hasRequiredFields() {
        return StringUtils.hasText(bootstrapAdminProperties.getEmail()) &&
                StringUtils.hasText(bootstrapAdminProperties.getPassword()) &&
                StringUtils.hasText(bootstrapAdminProperties.getName()) &&
                StringUtils.hasText(bootstrapAdminProperties.getSurname());
    }

    private void createBootstrapAdmin() {
        User adminUser = new User();
        adminUser.setEmail(bootstrapAdminProperties.getEmail());
        adminUser.setPassword(passwordEncoder.encode(bootstrapAdminProperties.getPassword()));
        adminUser.setName(bootstrapAdminProperties.getName());
        adminUser.setSurname(bootstrapAdminProperties.getSurname());
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);

        userRepository.save(adminUser);
        logger.info("Bootstrap admin created successfully for email: {}", adminUser.getEmail());
    }
}
