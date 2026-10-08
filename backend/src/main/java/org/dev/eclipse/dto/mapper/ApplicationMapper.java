package org.dev.eclipse.dto.mapper;

import org.dev.eclipse.dto.response.ApplicationResponse;
import org.dev.eclipse.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ApplicationMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "supplierOrganizationId", source = "supplierOrganization.id")
    @Mapping(target = "supplierOrganizationName", source = "supplierOrganization.name")
    ApplicationResponse toResponse(Application application);
}
