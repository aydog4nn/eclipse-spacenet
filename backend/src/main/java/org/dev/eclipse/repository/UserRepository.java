package org.dev.eclipse.repository;

import org.dev.eclipse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String userEmail);

    Optional<User> findByUsername(String username);

    Optional<User> findByOrganization_Id(UUID organizationId);

    User saveAndFlush(User user);
}
