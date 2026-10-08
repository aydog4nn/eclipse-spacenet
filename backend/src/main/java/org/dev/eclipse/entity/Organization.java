package org.dev.eclipse.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.dev.eclipse.enumarate.OnboardingStep;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.enumarate.SupplierCategory;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations")
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SupplierCategory supplierCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logoUrl;

    private String activityArea;

    private String location;

    private String website;

    private String phoneNumber;

    private String contactEmail;

    private Integer teamSize;

    private boolean verified;

    private boolean approved;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OnboardingStep onboardingStep = OnboardingStep.BASIC_INFO;

    private boolean onboardingCompleted;

    private boolean visible = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_focus_areas", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "focus_area", length = 100)
    private List<String> focusAreas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_certifications", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "certification", length = 150)
    private List<String> certifications = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_reference_projects", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "reference_project", length = 200)
    private List<String> referenceProjects = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_certificate_files", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "certificate_file_url", length = 255)
    private List<String> certificateFileUrls = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_certificate_labels", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "certificate_label", length = 180)
    private List<String> certificateFileLabels = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_expertise_areas", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "expertise_area", length = 150)
    private List<String> expertiseAreas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_hardware_capabilities", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "hardware_capability", length = 150)
    private List<String> hardwareCapabilities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_software_capabilities", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "software_capability", length = 150)
    private List<String> softwareCapabilities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_rd_capabilities", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "rd_capability", length = 150)
    private List<String> rdCapabilities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_production_infrastructure", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "production_item", length = 150)
    private List<String> productionInfrastructure = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_test_infrastructure", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "test_item", length = 150)
    private List<String> testInfrastructure = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_service_areas", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "service_area", length = 150)
    private List<String> serviceAreas = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public SupplierCategory getSupplierCategory() {
        return supplierCategory;
    }

    public void setSupplierCategory(SupplierCategory supplierCategory) {
        this.supplierCategory = supplierCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getActivityArea() {
        return activityArea;
    }

    public void setActivityArea(String activityArea) {
        this.activityArea = activityArea;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public OnboardingStep getOnboardingStep() {
        return onboardingStep;
    }

    public void setOnboardingStep(OnboardingStep onboardingStep) {
        this.onboardingStep = onboardingStep;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<String> getFocusAreas() {
        return focusAreas;
    }

    public void setFocusAreas(List<String> focusAreas) {
        this.focusAreas = focusAreas == null ? new ArrayList<>() : new ArrayList<>(focusAreas);
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications == null ? new ArrayList<>() : new ArrayList<>(certifications);
    }

    public List<String> getReferenceProjects() {
        return referenceProjects;
    }

    public void setReferenceProjects(List<String> referenceProjects) {
        this.referenceProjects = referenceProjects == null ? new ArrayList<>() : new ArrayList<>(referenceProjects);
    }

    public List<String> getCertificateFileUrls() {
        return certificateFileUrls;
    }

    public void setCertificateFileUrls(List<String> certificateFileUrls) {
        this.certificateFileUrls = certificateFileUrls == null ? new ArrayList<>() : new ArrayList<>(certificateFileUrls);
    }

    public List<String> getCertificateFileLabels() {
        return certificateFileLabels;
    }

    public void setCertificateFileLabels(List<String> certificateFileLabels) {
        this.certificateFileLabels = certificateFileLabels == null ? new ArrayList<>() : new ArrayList<>(certificateFileLabels);
    }

    public List<String> getExpertiseAreas() {
        return expertiseAreas;
    }

    public void setExpertiseAreas(List<String> expertiseAreas) {
        this.expertiseAreas = expertiseAreas == null ? new ArrayList<>() : new ArrayList<>(expertiseAreas);
    }

    public List<String> getHardwareCapabilities() {
        return hardwareCapabilities;
    }

    public void setHardwareCapabilities(List<String> hardwareCapabilities) {
        this.hardwareCapabilities = hardwareCapabilities == null ? new ArrayList<>() : new ArrayList<>(hardwareCapabilities);
    }

    public List<String> getSoftwareCapabilities() {
        return softwareCapabilities;
    }

    public void setSoftwareCapabilities(List<String> softwareCapabilities) {
        this.softwareCapabilities = softwareCapabilities == null ? new ArrayList<>() : new ArrayList<>(softwareCapabilities);
    }

    public List<String> getRdCapabilities() {
        return rdCapabilities;
    }

    public void setRdCapabilities(List<String> rdCapabilities) {
        this.rdCapabilities = rdCapabilities == null ? new ArrayList<>() : new ArrayList<>(rdCapabilities);
    }

    public List<String> getProductionInfrastructure() {
        return productionInfrastructure;
    }

    public void setProductionInfrastructure(List<String> productionInfrastructure) {
        this.productionInfrastructure = productionInfrastructure == null ? new ArrayList<>() : new ArrayList<>(productionInfrastructure);
    }

    public List<String> getTestInfrastructure() {
        return testInfrastructure;
    }

    public void setTestInfrastructure(List<String> testInfrastructure) {
        this.testInfrastructure = testInfrastructure == null ? new ArrayList<>() : new ArrayList<>(testInfrastructure);
    }

    public List<String> getServiceAreas() {
        return serviceAreas;
    }

    public void setServiceAreas(List<String> serviceAreas) {
        this.serviceAreas = serviceAreas == null ? new ArrayList<>() : new ArrayList<>(serviceAreas);
    }
}
