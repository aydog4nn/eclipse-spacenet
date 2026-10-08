package org.dev.eclipse.dto.mapper;

import org.dev.eclipse.dto.request.CapabilityRequest;
import org.dev.eclipse.dto.response.CapabilityResponse;
import org.dev.eclipse.entity.Capability;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface CapabilityMapper {

    @Mapping(target = "name", expression = "java(request.name().trim())")
    @Mapping(target = "description", expression = "java(trimToNull(request.description()))")
    @Mapping(target = "active", expression = "java(request.active() == null || request.active())")
    Capability toEntity(CapabilityRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", expression = "java(request.name().trim())")
    @Mapping(target = "description", expression = "java(trimToNull(request.description()))")
    @Mapping(target = "active", source = "active")
    void updateEntity(@MappingTarget Capability capability, CapabilityRequest request);

    CapabilityResponse toResponse(Capability capability);

    default String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
