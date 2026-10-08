package org.dev.eclipse.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public record AnnouncementResponse(
        UUID id,
        String title,
        String subtitle,
        String content,
        String imagePath,
        List<String> hashtags,
        LocalDateTime publishDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
