package org.dev.eclipse.service.impl;

import org.dev.eclipse.annotation.Audit;
import org.dev.eclipse.dto.mapper.UserMapper;
import org.dev.eclipse.dto.request.ChangeUserRoleRequest;
import org.dev.eclipse.dto.request.FirmRegisterRequest;
import org.dev.eclipse.dto.request.OrganizationApprovalUpdateRequest;
import org.dev.eclipse.dto.request.UserSearchRequest;
import org.dev.eclipse.dto.response.UserResponse;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.AuthExceptions.AuthException;
import org.dev.eclipse.exception.UserExceptions.UserNotFoundException;
import org.dev.eclipse.repository.PasswordResetTokenRepository;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.AdminUserService;
import org.dev.eclipse.service.OrganizationSearchService;
import org.dev.eclipse.service.async.AsyncMailDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;
import java.util.List;

@Service
@Validated
public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsyncMailDispatcher asyncMailDispatcher;
    private final OrganizationSearchService organizationSearchService;
    private final UserMapper userMapper;

    public AdminUserServiceImpl(UserRepository userRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder,
                                AsyncMailDispatcher asyncMailDispatcher,
                                OrganizationSearchService organizationSearchService,
                                UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.asyncMailDispatcher = asyncMailDispatcher;
        this.organizationSearchService = organizationSearchService;
        this.userMapper = userMapper;
    }

    @Override
    @Audit(action = "GET_ALL_USERS", resource = "User")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        logger.info("All users fetched, count: {}", users.size());
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Audit(action = "DELETE_USER", resource = "User", details = "Hard delete by admin")
    public void hardDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        passwordResetTokenRepository.deleteByUser(user);
        if (user.getOrganization() != null) {
            organizationSearchService.removeOrganization(user.getOrganization().getId());
        }
        userRepository.delete(user);

        logger.info("User with id {} has been hard deleted by admin.", userId);
    }

    @Override
    @Transactional
    @Audit(action = "REGISTER_USER", resource = "User")
    public UserResponse register(FirmRegisterRequest request) {
        logger.info("Register request received for email: {}", request.email());

        userRepository.findByEmail(request.email()).ifPresent(existingUser -> {
            throw new AuthException("Email is already registered");
        });

        userRepository.findByUsername(request.username()).ifPresent(existingUser -> {
            throw new AuthException("Username is already registered");
        });

        User user = userMapper.toFirmEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);

        userRepository.saveAndFlush(user);
        organizationSearchService.indexOrganization(user.getOrganization());

        String welcomeName = user.getOrganization() != null ? user.getOrganization().getName() : user.getEmail();
        asyncMailDispatcher.sendWelcomeEmail(user.getEmail(), welcomeName);

        logger.info("User registered and welcome email queued: {}", request.email());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @Audit(action = "CHANGE_USER_ROLE", resource = "User", details = "Changing role by admin")
    public void changeUserRole(UUID userId, ChangeUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Role newRole = request.role();

        user.setRole(newRole);
        if (user.getOrganization() != null) {
            if (newRole == Role.ADMIN) {
                organizationSearchService.removeOrganization(user.getOrganization().getId());
            } else {
                user.getOrganization().setRole(newRole);
            }
        }
        userRepository.save(user);
        if (user.getOrganization() != null && newRole != Role.ADMIN) {
            organizationSearchService.indexOrganization(user.getOrganization());
        }

        logger.info("User with id {} role changed to {} by admin.", userId, newRole);
    }

    @Override
    @Transactional
    @Audit(action = "UPDATE_ORGANIZATION_APPROVAL", resource = "Organization")
    public void updateOrganizationApproval(UUID userId, OrganizationApprovalUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getOrganization() == null) {
            throw new UserNotFoundException("Organization not found for user");
        }

        user.getOrganization().setApproved(request.approved());
        userRepository.save(user);
        organizationSearchService.indexOrganization(user.getOrganization());

        logger.info("Organization approval updated for userId={} approved={}", userId, request.approved());
    }

    @Override
    @Audit(action = "SEARCH_USERS", resource = "User")
    public List<UserResponse> searchUsers(UserSearchRequest request) {
        String value = request.value();
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String lowerValue = value.toLowerCase();

        List<User> allUsers = userRepository.findAll();

        List<User> filtered = allUsers.stream()
                .filter(user -> {
                    Organization organization = user.getOrganization();
                    String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                    String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                    String name = user.getName() != null ? user.getName().toLowerCase() : "";
                    String surname = user.getSurname() != null ? user.getSurname().toLowerCase() : "";
                    String organizationName = organization != null && organization.getName() != null ? organization.getName().toLowerCase() : "";
                    String phoneNumber = organization != null && organization.getPhoneNumber() != null ? organization.getPhoneNumber().toLowerCase() : "";
                    String location = organization != null && organization.getLocation() != null ? organization.getLocation().toLowerCase() : "";
                    String focusAreas = organization != null ? String.join(" ", organization.getFocusAreas()).toLowerCase() : "";
                    String supplierCategory = organization != null && organization.getSupplierCategory() != null ? organization.getSupplierCategory().name().toLowerCase() : "";
                    String role = user.getRole() != null ? user.getRole().name().toLowerCase() : "";
                    String activeStr = String.valueOf(user.isActive()).toLowerCase();

                    return email.contains(lowerValue) ||
                            username.contains(lowerValue) ||
                            name.contains(lowerValue) ||
                            surname.contains(lowerValue) ||
                            organizationName.contains(lowerValue) ||
                            phoneNumber.contains(lowerValue) ||
                            location.contains(lowerValue) ||
                            focusAreas.contains(lowerValue) ||
                            supplierCategory.contains(lowerValue) ||
                            role.contains(lowerValue) ||
                            activeStr.equals(lowerValue);
                })
                .toList();

        logger.info("Search for '{}' returned {} results", value, filtered.size());

        return filtered.stream()
                .map(userMapper::toResponse)
                .toList();
    }

}
