package org.dev.eclipse.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.dev.eclipse.enumarate.CollaborationType;
import org.dev.eclipse.enumarate.ConfidentialityLevel;
import org.dev.eclipse.enumarate.ProjectStatus;
import org.dev.eclipse.enumarate.SupplierCategory;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String projectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private CollaborationType collaborationType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_capabilities",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private Set<Capability> requiredCapabilities = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 80)
    private SupplierCategory preferredSupplierCategory;

    private String locationPreference;

    private String durationText;

    private String budgetRange;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_certification_requirements", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "certification_requirement", length = 150)
    private Set<String> certificationRequirements = new LinkedHashSet<>();

    private LocalDate applicationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConfidentialityLevel confidentialityLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectStatus status = ProjectStatus.TASLAK;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public CollaborationType getCollaborationType() {
        return collaborationType;
    }

    public void setCollaborationType(CollaborationType collaborationType) {
        this.collaborationType = collaborationType;
    }

    public Set<Capability> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void setRequiredCapabilities(Set<Capability> requiredCapabilities) {
        this.requiredCapabilities = requiredCapabilities;
    }

    public SupplierCategory getPreferredSupplierCategory() {
        return preferredSupplierCategory;
    }

    public void setPreferredSupplierCategory(SupplierCategory preferredSupplierCategory) {
        this.preferredSupplierCategory = preferredSupplierCategory;
    }

    public String getLocationPreference() {
        return locationPreference;
    }

    public void setLocationPreference(String locationPreference) {
        this.locationPreference = locationPreference;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(String budgetRange) {
        this.budgetRange = budgetRange;
    }

    public Set<String> getCertificationRequirements() {
        return certificationRequirements;
    }

    public void setCertificationRequirements(Set<String> certificationRequirements) {
        this.certificationRequirements = certificationRequirements;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public ConfidentialityLevel getConfidentialityLevel() {
        return confidentialityLevel;
    }

    public void setConfidentialityLevel(ConfidentialityLevel confidentialityLevel) {
        this.confidentialityLevel = confidentialityLevel;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
