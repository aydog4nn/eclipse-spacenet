package org.dev.eclipse.controller;

import jakarta.validation.Valid;
import org.dev.eclipse.dto.request.InvitationRequest;
import org.dev.eclipse.dto.request.InvitationStatusUpdateRequest;
import org.dev.eclipse.dto.response.InvitationResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<RestResponse<InvitationResponse>> sendInvitation(@PathVariable UUID projectId,
                                                                           @Valid @RequestBody InvitationRequest request) {
        return ResponseEntity.ok(RestResponse.success("Invitation sent", invitationService.sendInvitation(projectId, request)));
    }

    @GetMapping("/my")
    public ResponseEntity<RestResponse<List<InvitationResponse>>> getMyInvitations() {
        return ResponseEntity.ok(RestResponse.success("Invitations listed", invitationService.getMyInvitations()));
    }

    @PatchMapping("/{invitationId}/respond")
    public ResponseEntity<RestResponse<InvitationResponse>> respondToInvitation(@PathVariable UUID invitationId,
                                                                                @Valid @RequestBody InvitationStatusUpdateRequest request) {
        return ResponseEntity.ok(RestResponse.success("Invitation status updated", invitationService.respondToInvitation(invitationId, request)));
    }
}
