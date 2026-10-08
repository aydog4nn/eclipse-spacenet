package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.InvitationRequest;
import org.dev.eclipse.dto.request.InvitationStatusUpdateRequest;
import org.dev.eclipse.dto.response.InvitationResponse;

import java.util.List;
import java.util.UUID;

public interface InvitationService {

    InvitationResponse sendInvitation(UUID projectId, InvitationRequest request);

    List<InvitationResponse> getMyInvitations();

    InvitationResponse respondToInvitation(UUID invitationId, InvitationStatusUpdateRequest request);
}
