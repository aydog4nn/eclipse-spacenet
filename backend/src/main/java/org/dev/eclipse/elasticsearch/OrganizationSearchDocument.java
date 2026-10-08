package org.dev.eclipse.elasticsearch;

import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.enumarate.SupplierCategory;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(indexName = "organization_search")
public class OrganizationSearchDocument extends ElasticBaseDocument {

    @Id
    private String id;

    private UUID organizationId;
    private Role role;
    private SupplierCategory supplierCategory;
    private String name;
    private String description;
    private String location;
    private String website;
    private boolean verified;
    private boolean approved;
    private boolean onboardingCompleted;
    private boolean visible;
    private List<String> focusAreas = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();
    private List<String> referenceProjects = new ArrayList<>();
    private List<String> expertiseAreas = new ArrayList<>();
    private List<String> hardwareCapabilities = new ArrayList<>();
    private List<String> softwareCapabilities = new ArrayList<>();
    private List<String> rdCapabilities = new ArrayList<>();
    private List<String> productionInfrastructure = new ArrayList<>();
    private List<String> testInfrastructure = new ArrayList<>();
    private List<String> serviceAreas = new ArrayList<>();
    private Integer teamSize;
    private String searchText;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
