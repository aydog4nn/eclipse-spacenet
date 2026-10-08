package org.dev.eclipse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.dev.eclipse.enumarate.ApplicationStatus;

@Entity
@Table(name = "applications")
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "supplier_organization_id", nullable = false)
    private Organization supplierOrganization;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String whyFit;

    private String documentUrl;

    private String referenceProjectNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus status = ApplicationStatus.GONDERILDI;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Organization getSupplierOrganization() {
        return supplierOrganization;
    }

    public void setSupplierOrganization(Organization supplierOrganization) {
        this.supplierOrganization = supplierOrganization;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getWhyFit() {
        return whyFit;
    }

    public void setWhyFit(String whyFit) {
        this.whyFit = whyFit;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getReferenceProjectNote() {
        return referenceProjectNote;
    }

    public void setReferenceProjectNote(String referenceProjectNote) {
        this.referenceProjectNote = referenceProjectNote;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
