package org.dev.eclipse.service.impl;

import org.dev.eclipse.dto.mapper.CapabilityMapper;
import org.dev.eclipse.dto.request.CapabilityRequest;
import org.dev.eclipse.dto.response.CapabilityResponse;
import org.dev.eclipse.entity.Capability;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.repository.CapabilityRepository;
import org.dev.eclipse.service.CapabilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CapabilityServiceImpl implements CapabilityService {

    private final CapabilityRepository capabilityRepository;
    private final CapabilityMapper capabilityMapper;

    public CapabilityServiceImpl(CapabilityRepository capabilityRepository,
                                 CapabilityMapper capabilityMapper) {
        this.capabilityRepository = capabilityRepository;
        this.capabilityMapper = capabilityMapper;
    }

    @Override
    public List<CapabilityResponse> getAllActiveCapabilities() {
        return capabilityRepository.findByActiveTrueOrderByCapabilityGroupAscNameAsc().stream()
                .map(capabilityMapper::toResponse)
                .toList();
    }

    @Override
    public List<CapabilityResponse> getAllCapabilitiesForAdmin() {
        return capabilityRepository.findAll().stream()
                .map(capabilityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CapabilityResponse createCapability(CapabilityRequest request) {
        capabilityRepository.findByNameIgnoreCase(request.name().trim()).ifPresent(existing -> {
            throw new BadRequestException("Bu yetkinlik zaten mevcut.");
        });

        Capability capability = capabilityMapper.toEntity(request);
        return capabilityMapper.toResponse(capabilityRepository.save(capability));
    }

    @Override
    @Transactional
    public CapabilityResponse updateCapability(UUID capabilityId, CapabilityRequest request) {
        Capability capability = capabilityRepository.findById(capabilityId)
                .orElseThrow(() -> new BadRequestException("Yetkinlik bulunamadi."));

        capabilityRepository.findByNameIgnoreCase(request.name().trim())
                .filter(existing -> !existing.getId().equals(capabilityId))
                .ifPresent(existing -> {
                    throw new BadRequestException("Bu yetkinlik adi baska bir kayitta kullaniliyor.");
                });

        capabilityMapper.updateEntity(capability, request);
        return capabilityMapper.toResponse(capabilityRepository.save(capability));
    }

    @Override
    @Transactional
    public void deleteCapability(UUID capabilityId) {
        Capability capability = capabilityRepository.findById(capabilityId)
                .orElseThrow(() -> new BadRequestException("Yetkinlik bulunamadi."));
        capabilityRepository.delete(capability);
    }
}
