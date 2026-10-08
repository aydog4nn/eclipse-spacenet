package org.dev.eclipse.service.impl;

import org.dev.eclipse.annotation.Audit;
import org.dev.eclipse.dto.mapper.AnnouncementMapper;
import org.dev.eclipse.dto.request.AnnouncementRequest;
import org.dev.eclipse.dto.response.AnnouncementResponse;
import org.dev.eclipse.entity.Announcement;
import org.dev.eclipse.exception.AnnouncementExceptions.AnnouncementNotFoundException;
import org.dev.eclipse.repository.AnnouncementRepository;
import org.dev.eclipse.service.AnnouncementService;
import org.dev.eclipse.util.FileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.List;

@Service
@Validated
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementServiceImpl.class);
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementMapper announcementMapper;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                   AnnouncementMapper announcementMapper) {
        this.announcementRepository = announcementRepository;
        this.announcementMapper = announcementMapper;
    }

    @Override
    @Audit(action = "GET_ALL_ANNOUNCEMENTS", resource = "Announcement")
    public List<AnnouncementResponse> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAll();
        logger.info("Fetched all announcements, count: {}", announcements.size());
        return announcements.stream()
                .map(announcementMapper::toResponse)
                .toList();
    }

    @Override
    @Audit(action = "GET_ANNOUNCEMENT_BY_ID", resource = "Announcement")
    public AnnouncementResponse getAnnouncementById(UUID announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AnnouncementNotFoundException("Duyuru bulunamadı."));
        logger.info("Fetched announcement with id {}", announcementId);
        return announcementMapper.toResponse(announcement);
    }

    @Override
    @Transactional
    @Audit(action = "CREATE_ANNOUNCEMENT", resource = "Announcement")
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, MultipartFile file) throws IOException {
        logger.info("Creating new announcement: {}", request.title());

        String imagePath = FileUploadUtil.saveSingleFile(file, "announcements");

        Announcement announcement = announcementMapper.toEntity(request);
        announcement.setImagePath(imagePath);

        Announcement saved = announcementRepository.save(announcement);
        logger.info("Announcement created successfully with id {}", saved.getId());

        return announcementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Audit(action = "UPDATE_ANNOUNCEMENT", resource = "Announcement")
    public AnnouncementResponse updateAnnouncement(UUID announcementId, AnnouncementRequest request, MultipartFile file) throws IOException {
        Announcement existingAnnouncement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AnnouncementNotFoundException("Duyuru bulunamadı."));

        String imagePath = FileUploadUtil.saveSingleFile(file, "announcements");

        announcementMapper.updateFromRequest(existingAnnouncement, request);
        if (imagePath != null && !imagePath.isBlank()) {
            existingAnnouncement.setImagePath(imagePath);
        }

        Announcement updated = announcementRepository.save(existingAnnouncement);
        logger.info("Announcement with id {} updated successfully (partial update).", announcementId);

        return announcementMapper.toResponse(updated);
    }


    @Override
    @Transactional
    @Audit(action = "DELETE_ANNOUNCEMENT", resource = "Announcement")
    public void deleteAnnouncement(UUID announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AnnouncementNotFoundException("Duyuru bulunamadı."));

        announcementRepository.delete(announcement);
        logger.info("Announcement with id {} deleted successfully.", announcementId);
    }

    @Override
    @Audit(action = "SEARCH_ANNOUNCEMENT_BY_TITLE", resource = "Announcement")
    public List<AnnouncementResponse> searchByTitle(String title) {
        List<Announcement> announcements = announcementRepository.findByTitleContainingIgnoreCase(title);
        logger.info("Search by title '{}' returned {} results", title, announcements.size());
        return announcements.stream()
                .map(announcementMapper::toResponse)
                .toList();
    }

    @Override
    @Audit(action = "SEARCH_ANNOUNCEMENT_BY_HASHTAG", resource = "Announcement")
    public List<AnnouncementResponse> searchByHashtag(String hashtag) {
        List<Announcement> announcements = announcementRepository.findByHashtagsContaining(hashtag);
        logger.info("Search by hashtag '{}' returned {} results", hashtag, announcements.size());
        return announcements.stream()
                .map(announcementMapper::toResponse)
                .toList();
    }

    @Override
    @Audit(action = "GET_LATEST_FIVE_ANNOUNCEMENTS", resource = "Announcement")
    public List<AnnouncementResponse> getLatestFiveAnnouncements() {
        List<Announcement> latestAnnouncements = announcementRepository.findTop5ByOrderByCreatedAtDesc();
        logger.info("Fetched latest 5 announcements (sorted by createdAt desc), count: {}", latestAnnouncements.size());

        return latestAnnouncements.stream()
                .map(announcementMapper::toResponse)
                .toList();
    }

}
