package org.dev.eclipse.repository;

import org.dev.eclipse.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    List<Announcement> findByTitleContainingIgnoreCase(String title);

    List<Announcement> findByHashtagsContaining(String hashtag);

    @Query("SELECT a FROM Announcement a ORDER BY a.createdAt DESC")
    List<Announcement> findTop5ByOrderByCreatedAtDesc();
}
