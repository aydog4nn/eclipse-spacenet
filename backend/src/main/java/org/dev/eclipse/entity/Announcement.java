package org.dev.eclipse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_path")
    private String imagePath;

    @ElementCollection
    @CollectionTable(name = "announcement_hashtags", joinColumns = @JoinColumn(name = "announcement_id"))
    @Column(name = "hashtag", length = 50)
    private List<String> hashtags = new ArrayList<>();

    @Column(name = "publish_date", nullable = false)
    private LocalDateTime publishDate = LocalDateTime.now();

    public Announcement() {}

    public Announcement(String title, String subtitle, String content, String imagePath, List<String> hashtags, LocalDateTime publishDate) {
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.imagePath = imagePath;
        this.hashtags = hashtags;
        this.publishDate = publishDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", content='" + content + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", hashtags=" + hashtags +
                ", publishDate=" + publishDate +
                '}';
    }
}
