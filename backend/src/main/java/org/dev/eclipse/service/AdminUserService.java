package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.ChangeUserRoleRequest;
import org.dev.eclipse.dto.request.FirmRegisterRequest;
import org.dev.eclipse.dto.request.OrganizationApprovalUpdateRequest;
import org.dev.eclipse.dto.request.UserSearchRequest;
import org.dev.eclipse.dto.response.UserResponse;

import java.util.UUID;
import java.util.List;

public interface AdminUserService {

    List<UserResponse> getAllUsers();

    void hardDeleteUser(UUID userId);

    void changeUserRole(UUID userId, ChangeUserRoleRequest request);

    void updateOrganizationApproval(UUID userId, OrganizationApprovalUpdateRequest request);

    List<UserResponse> searchUsers(UserSearchRequest request);

    UserResponse register(FirmRegisterRequest request);


}
