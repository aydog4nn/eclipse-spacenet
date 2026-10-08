package org.dev.eclipse.service.impl;

import org.dev.eclipse.exception.AuthExceptions.AuthException;
import org.dev.eclipse.exception.AuthExceptions.InactiveUserException;
import org.dev.eclipse.exception.AuthExceptions.UserNotFoundException;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    private final UserRepository userRepository;

    public SecurityServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.error("Authentication information not found or user is anonymous.");
            throw new AuthException("User is not authenticated.");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(user -> {
                    if (!user.isActive()) {
                        logger.error("User is inactive: {}", email);
                        throw new InactiveUserException("User is not active.");
                    }
                    if ((user.getRole() == Role.FIRMA || user.getRole() == Role.TEDARIKCI)
                            && (user.getOrganization() == null || !user.getOrganization().isApproved())) {
                        logger.error("Organization approval pending for user: {}", email);
                        throw new AuthException("Organization approval is pending.");
                    }
                    return user.getId();
                })
                .orElseThrow(() -> {
                    logger.error("User not found in the database: {}", email);
                    return new UserNotFoundException("User not found.");
                });
    }

}
