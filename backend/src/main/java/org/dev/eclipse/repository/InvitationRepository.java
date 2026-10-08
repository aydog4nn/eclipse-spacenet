package org.dev.eclipse.repository;

import org.dev.eclipse.entity.Invitation;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    List<Invitation> findBySupplierOrganizationOrderByCreatedAtDesc(Organization supplierOrganization);

    Optional<Invitation> findByProjectAndSupplierOrganization(Project project, Organization supplierOrganization);
}
