package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;

public record AnnouncementRequest(

        @Size(max = 200, message = "Başlık 200 karakteri geçemez")
        String title,

        @Size(max = 300, message = "Alt başlık 300 karakteri geçemez")
        String subtitle,

        String content,

        List<String> hashtags

) { }
