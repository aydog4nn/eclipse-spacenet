package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.AnnouncementRequest;
import org.dev.eclipse.dto.response.AnnouncementResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.List;

public interface AnnouncementService {

    List<AnnouncementResponse> getAllAnnouncements();

    AnnouncementResponse getAnnouncementById(UUID announcementId);

    AnnouncementResponse createAnnouncement(AnnouncementRequest request, MultipartFile file) throws IOException;

    AnnouncementResponse updateAnnouncement(UUID announcementId, AnnouncementRequest request, MultipartFile file) throws IOException;

    void deleteAnnouncement(UUID announcementId);

    List<AnnouncementResponse> searchByTitle(String title);

    List<AnnouncementResponse> searchByHashtag(String hashtag);

    List<AnnouncementResponse> getLatestFiveAnnouncements();

}
