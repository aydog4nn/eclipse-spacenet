package org.dev.eclipse.repository;

import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.dev.eclipse.enumarate.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);
}
