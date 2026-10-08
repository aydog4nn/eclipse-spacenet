package org.dev.eclipse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.dev.eclipse.enumarate.CapabilityGroup;

@Entity
@Table(name = "capabilities")
public class Capability extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CapabilityGroup capabilityGroup;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CapabilityGroup getCapabilityGroup() {
        return capabilityGroup;
    }

    public void setCapabilityGroup(CapabilityGroup capabilityGroup) {
        this.capabilityGroup = capabilityGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
