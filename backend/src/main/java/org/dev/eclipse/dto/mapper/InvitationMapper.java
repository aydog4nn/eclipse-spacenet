package org.dev.eclipse.dto.mapper;

import org.dev.eclipse.dto.response.InvitationResponse;
import org.dev.eclipse.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface InvitationMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "supplierOrganizationId", source = "supplierOrganization.id")
    @Mapping(target = "supplierOrganizationName", source = "supplierOrganization.name")
    InvitationResponse toResponse(Invitation invitation);
}
