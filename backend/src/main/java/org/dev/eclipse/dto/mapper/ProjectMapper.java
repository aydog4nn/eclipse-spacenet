package org.dev.eclipse.dto.mapper;

import org.dev.eclipse.dto.response.ProjectResponse;
import org.dev.eclipse.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = CapabilityMapper.class)
public interface ProjectMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "requiredCapabilities", source = "requiredCapabilities")
    @Mapping(target = "certificationRequirements", expression = "java(project.getCertificationRequirements().stream().toList())")
    ProjectResponse toResponse(Project project);
}
