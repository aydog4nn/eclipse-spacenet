package org.dev.eclipse.repository;

import org.dev.eclipse.entity.Capability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapabilityRepository extends JpaRepository<Capability, UUID> {
    Optional<Capability> findByNameIgnoreCase(String name);

    List<Capability> findByActiveTrueOrderByCapabilityGroupAscNameAsc();
}
