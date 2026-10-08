package org.dev.eclipse.dto.mapper;

import org.dev.eclipse.dto.request.AnnouncementRequest;
import org.dev.eclipse.dto.response.AnnouncementResponse;
import org.dev.eclipse.entity.Announcement;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface AnnouncementMapper {

    @Mapping(target = "title", expression = "java(trimToNull(request.title()))")
    @Mapping(target = "subtitle", expression = "java(trimToNull(request.subtitle()))")
    @Mapping(target = "content", expression = "java(trimToNull(request.content()))")
    @Mapping(target = "hashtags", expression = "java(normalizeList(request.hashtags()))")
    Announcement toEntity(AnnouncementRequest request);

    AnnouncementResponse toResponse(Announcement announcement);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "title", expression = "java(keepOrReplace(announcement.getTitle(), request.title()))")
    @Mapping(target = "subtitle", expression = "java(keepOrReplace(announcement.getSubtitle(), request.subtitle()))")
    @Mapping(target = "content", expression = "java(keepOrReplace(announcement.getContent(), request.content()))")
    @Mapping(target = "hashtags", expression = "java(keepOrReplaceList(announcement.getHashtags(), request.hashtags()))")
    void updateFromRequest(@MappingTarget Announcement announcement, AnnouncementRequest request);

    default String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    default List<String> normalizeList(List<String> values) {
        return values == null ? List.of() : values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    default String keepOrReplace(String currentValue, String requestedValue) {
        String normalized = trimToNull(requestedValue);
        return normalized == null ? currentValue : normalized;
    }

    default List<String> keepOrReplaceList(List<String> currentValue, List<String> requestedValue) {
        List<String> normalized = normalizeList(requestedValue);
        return normalized.isEmpty() ? currentValue : normalized;
    }
}
