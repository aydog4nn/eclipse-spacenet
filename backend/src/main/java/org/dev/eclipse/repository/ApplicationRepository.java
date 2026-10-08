package org.dev.eclipse.repository;

import org.dev.eclipse.entity.Application;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findBySupplierOrganizationOrderByCreatedAtDesc(Organization supplierOrganization);

    List<Application> findByProjectOrderByCreatedAtDesc(Project project);

    Optional<Application> findByProjectAndSupplierOrganization(Project project, Organization supplierOrganization);
}
