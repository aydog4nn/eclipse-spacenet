package org.dev.eclipse.service.impl;

import org.dev.eclipse.dto.mapper.ProjectMapper;
import org.dev.eclipse.dto.request.SupplierSearchRequest;
import org.dev.eclipse.dto.response.ProjectMatchResponse;
import org.dev.eclipse.dto.response.SupplierSearchResponse;
import org.dev.eclipse.elasticsearch.OrganizationSearchDocument;
import org.dev.eclipse.elasticsearch.OrganizationSearchRepository;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.ProjectStatus;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.exception.UserExceptions.UserNotFoundException;
import org.dev.eclipse.repository.OrganizationRepository;
import org.dev.eclipse.repository.ProjectRepository;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.OrganizationSearchService;
import org.dev.eclipse.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class OrganizationSearchServiceImpl implements OrganizationSearchService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationSearchServiceImpl.class);

    private final OrganizationSearchRepository organizationSearchRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final ProjectMapper projectMapper;

    public OrganizationSearchServiceImpl(OrganizationSearchRepository organizationSearchRepository,
                                         OrganizationRepository organizationRepository,
                                         ProjectRepository projectRepository,
                                         UserRepository userRepository,
                                         SecurityService securityService,
                                         ProjectMapper projectMapper) {
        this.organizationSearchRepository = organizationSearchRepository;
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.projectMapper = projectMapper;
    }

    @Override
    public void indexOrganization(Organization organization) {
        if (organization == null || organization.getId() == null) {
            return;
        }

        if (organization.getRole() == Role.ADMIN) {
            removeOrganization(organization.getId());
            return;
        }
        OrganizationSearchDocument document = mapToDocument(organization);
        saveDocument(document);
    }

    @Override
    public void removeOrganization(UUID organizationId) {
        if (organizationId == null) {
            return;
        }
        try {
            organizationSearchRepository.deleteById(organizationId.toString());
        } catch (Exception ex) {
            logger.warn("Organization search index delete failed for org {}. Reason: {}", organizationId, ex.getMessage());
        }
    }

    @Override
    public List<SupplierSearchResponse> searchSuppliers(SupplierSearchRequest request) {
        return loadSupplierDocuments().stream()
                .filter(document -> document.getRole() == Role.TEDARIKCI)
                .filter(OrganizationSearchDocument::isApproved)
                .filter(OrganizationSearchDocument::isOnboardingCompleted)
                .filter(OrganizationSearchDocument::isVisible)
                .map(document -> scoreDocument(document, request, null))
                .filter(result -> result.matchScore() > 0 || isBlankSearch(request))
                .sorted(Comparator.comparingDouble(SupplierSearchResponse::matchScore).reversed()
                        .thenComparing(SupplierSearchResponse::organizationName))
                .toList();
    }

    @Override
    public List<SupplierSearchResponse> getSupplierMatchesForProject(UUID projectId) {
        User user = getAuthenticatedUser();
        if (user.getRole() != Role.FIRMA) {
            throw new BadRequestException("Bu islem sadece firma hesaplari icindir.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Ilan bulunamadi."));

        if (user.getOrganization() == null || !project.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new BadRequestException("Bu ilan icin eslesme goruntuleme yetkiniz yok.");
        }

        SupplierSearchRequest request = new SupplierSearchRequest(
                null,
                project.getPreferredSupplierCategory(),
                project.getLocationPreference(),
                null,
                null,
                null,
                null,
                null
        );

        return loadSupplierDocuments().stream()
                .filter(document -> document.getRole() == Role.TEDARIKCI)
                .filter(OrganizationSearchDocument::isApproved)
                .filter(OrganizationSearchDocument::isOnboardingCompleted)
                .filter(OrganizationSearchDocument::isVisible)
                .map(document -> scoreDocument(document, request, project))
                .sorted(Comparator.comparingDouble(SupplierSearchResponse::matchScore).reversed()
                        .thenComparing(SupplierSearchResponse::organizationName))
                .toList();
    }

    @Override
    public List<ProjectMatchResponse> getMatchingProjectsForCurrentSupplier() {
        User user = getAuthenticatedUser();
        if (user.getRole() != Role.TEDARIKCI) {
            throw new BadRequestException("Bu islem sadece tedarikci hesaplari icindir.");
        }
        Organization organization = requireOrganization(user);

        return projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.AKTIF).stream()
                .map(project -> scoreProjectForSupplier(project, organization))
                .filter(match -> match.matchScore() > 0)
                .sorted(Comparator.comparingDouble(ProjectMatchResponse::matchScore).reversed()
                        .thenComparing(match -> match.project().createdAt(), Comparator.reverseOrder()))
                .toList();
    }

    private SupplierSearchResponse scoreDocument(OrganizationSearchDocument document,
                                                 SupplierSearchRequest request,
                                                 Project project) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        if (request.keyword() != null && !request.keyword().isBlank()) {
            String keyword = request.keyword().toLowerCase(Locale.ROOT);
            if (document.getSearchText() != null && document.getSearchText().contains(keyword)) {
                score += 20;
                reasons.add("Anahtar kelime eslesmesi");
            } else {
                return toResponse(document, 0, List.of());
            }
        }

        if (request.supplierCategory() != null) {
            if (request.supplierCategory() == document.getSupplierCategory()) {
                score += 15;
                reasons.add("Alt kategori uyumu");
            } else {
                return toResponse(document, 0, List.of());
            }
        }

        if (request.location() != null && !request.location().isBlank()
                && document.getLocation() != null
                && document.getLocation().toLowerCase(Locale.ROOT).contains(request.location().toLowerCase(Locale.ROOT))) {
            score += 10;
            reasons.add("Lokasyon uyumu");
        }

        if (request.capability() != null && !request.capability().isBlank()) {
            String capability = request.capability().toLowerCase(Locale.ROOT);
            if (allCapabilityTexts(document).anyMatch(value -> value.contains(capability))) {
                score += 20;
                reasons.add("Yetkinlik eslesmesi");
            } else {
                return toResponse(document, 0, List.of());
            }
        }

        if (request.certification() != null && !request.certification().isBlank()) {
            String certification = request.certification().toLowerCase(Locale.ROOT);
            if (document.getCertifications().stream().map(value -> value.toLowerCase(Locale.ROOT)).anyMatch(value -> value.contains(certification))) {
                score += 10;
                reasons.add("Sertifika uyumu");
            } else {
                return toResponse(document, 0, List.of());
            }
        }

        if (Boolean.TRUE.equals(request.verifiedOnly())) {
            if (document.isVerified()) {
                score += 10;
                reasons.add("Dogrulanmis kurum");
            } else {
                return toResponse(document, 0, List.of());
            }
        }

        if (request.minTeamSize() != null) {
            if (document.getTeamSize() != null && document.getTeamSize() >= request.minTeamSize()) {
                score += 5;
                reasons.add("Minimum ekip buyuklugu uygun");
            } else {
                return toResponse(document, 0, List.of());
            }
        }

        if (request.maxTeamSize() != null) {
            if (document.getTeamSize() != null && document.getTeamSize() <= request.maxTeamSize()) {
                score += 5;
                reasons.add("Maksimum ekip buyuklugu uygun");
            } else {
                return toResponse(document, 0, List.of());
            }
        }

        if (project != null) {
            long capabilityMatches = project.getRequiredCapabilities().stream()
                    .map(capability -> capability.getName().toLowerCase(Locale.ROOT))
                    .filter(required -> allCapabilityTexts(document).anyMatch(value -> value.contains(required)))
                    .count();

            if (!project.getRequiredCapabilities().isEmpty()) {
                double capabilityScore = (capabilityMatches * 50.0) / project.getRequiredCapabilities().size();
                score += capabilityScore;
                if (capabilityMatches > 0) {
                    reasons.add("Proje yetkinlik uyumu");
                }
            }

            long certMatches = project.getCertificationRequirements().stream()
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .filter(required -> document.getCertifications().stream()
                            .map(cert -> cert.toLowerCase(Locale.ROOT))
                            .anyMatch(cert -> cert.contains(required)))
                    .count();

            if (!project.getCertificationRequirements().isEmpty() && certMatches > 0) {
                score += (certMatches * 15.0) / project.getCertificationRequirements().size();
                reasons.add("Sertifika gereksinimi uyumu");
            }

            if (project.getPreferredSupplierCategory() != null
                    && project.getPreferredSupplierCategory() == document.getSupplierCategory()) {
                score += 15;
                reasons.add("Tercih edilen alt kategori uyumu");
            }

            if (project.getLocationPreference() != null && document.getLocation() != null
                    && document.getLocation().toLowerCase(Locale.ROOT).contains(project.getLocationPreference().toLowerCase(Locale.ROOT))) {
                score += 10;
                reasons.add("Operasyonel lokasyon uyumu");
            }
        }

        return toResponse(document, roundScore(score), reasons);
    }

    private SupplierSearchResponse toResponse(OrganizationSearchDocument document, double score, List<String> reasons) {
        return new SupplierSearchResponse(
                document.getOrganizationId(),
                document.getName(),
                document.getSupplierCategory(),
                document.getDescription(),
                document.getLocation(),
                document.getExpertiseAreas(),
                document.getCertifications(),
                document.isVerified(),
                document.getTeamSize() == null ? 0 : document.getTeamSize(),
                score,
                reasons
        );
    }

    private ProjectMatchResponse scoreProjectForSupplier(Project project, Organization organization) {
        return scoreProjectForSupplier(
                project,
                buildSearchText(organization),
                copyList(organization.getCertifications()),
                organization.getSupplierCategory(),
                organization.getLocation()
        );
    }

    private ProjectMatchResponse scoreProjectForSupplier(Project project, OrganizationSearchDocument document) {
        return scoreProjectForSupplier(
                project,
                document.getSearchText(),
                document.getCertifications(),
                document.getSupplierCategory(),
                document.getLocation()
        );
    }

    private ProjectMatchResponse scoreProjectForSupplier(Project project,
                                                         String organizationText,
                                                         List<String> certifications,
                                                         org.dev.eclipse.enumarate.SupplierCategory supplierCategory,
                                                         String location) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        String normalizedSearchText = organizationText == null ? "" : organizationText.toLowerCase(Locale.ROOT);

        long capabilityMatches = project.getRequiredCapabilities().stream()
                .map(capability -> capability.getName().toLowerCase(Locale.ROOT))
                .filter(normalizedSearchText::contains)
                .count();

        if (!project.getRequiredCapabilities().isEmpty()) {
            double capabilityScore = (capabilityMatches * 50.0) / project.getRequiredCapabilities().size();
            score += capabilityScore;
            if (capabilityMatches > 0) {
                reasons.add("Yetkinlik uyumu");
            }
        }

        List<String> safeCertifications = certifications == null ? List.of() : certifications;
        long certificationMatches = project.getCertificationRequirements().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(required -> safeCertifications.stream()
                        .map(certification -> certification.toLowerCase(Locale.ROOT))
                        .anyMatch(certification -> certification.contains(required)))
                .count();

        if (!project.getCertificationRequirements().isEmpty() && certificationMatches > 0) {
            score += (certificationMatches * 15.0) / project.getCertificationRequirements().size();
            reasons.add("Sertifika uyumu");
        }

        if (project.getPreferredSupplierCategory() != null
                && project.getPreferredSupplierCategory() == supplierCategory) {
            score += 15;
            reasons.add("Alt kategori uyumu");
        }

        if (project.getLocationPreference() != null
                && location != null
                && location.toLowerCase(Locale.ROOT)
                .contains(project.getLocationPreference().toLowerCase(Locale.ROOT))) {
            score += 10;
            reasons.add("Lokasyon uyumu");
        }

        return new ProjectMatchResponse(projectMapper.toResponse(project), roundScore(score), reasons);
    }

    private User getAuthenticatedUser() {
        UUID userId = securityService.getAuthenticatedUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanici bulunamadi"));
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new BadRequestException("Kullaniciya bagli kurum profili bulunamadi.");
        }
        return user.getOrganization();
    }

    private boolean isBlankSearch(SupplierSearchRequest request) {
        return (request.keyword() == null || request.keyword().isBlank())
                && request.supplierCategory() == null
                && (request.location() == null || request.location().isBlank())
                && (request.capability() == null || request.capability().isBlank())
                && (request.certification() == null || request.certification().isBlank())
                && request.minTeamSize() == null
                && request.maxTeamSize() == null
                && request.verifiedOnly() == null;
    }

    private Stream<String> allCapabilityTexts(OrganizationSearchDocument document) {
        return Stream.of(
                        document.getFocusAreas(),
                        document.getExpertiseAreas(),
                        document.getHardwareCapabilities(),
                        document.getSoftwareCapabilities(),
                        document.getRdCapabilities(),
                        document.getProductionInfrastructure(),
                        document.getTestInfrastructure(),
                        document.getServiceAreas()
                )
                .flatMap(List::stream)
                .map(value -> value.toLowerCase(Locale.ROOT));
    }

    private String buildSearchText(Organization organization) {
        return Stream.of(
                        organization.getName(),
                        organization.getDescription(),
                        organization.getLocation(),
                        organization.getWebsite(),
                        organization.getActivityArea(),
                        String.join(" ", copyList(organization.getFocusAreas())),
                        String.join(" ", copyList(organization.getCertifications())),
                        String.join(" ", copyList(organization.getReferenceProjects())),
                        String.join(" ", copyList(organization.getExpertiseAreas())),
                        String.join(" ", copyList(organization.getHardwareCapabilities())),
                        String.join(" ", copyList(organization.getSoftwareCapabilities())),
                        String.join(" ", copyList(organization.getRdCapabilities())),
                        String.join(" ", copyList(organization.getProductionInfrastructure())),
                        String.join(" ", copyList(organization.getTestInfrastructure())),
                        String.join(" ", copyList(organization.getServiceAreas()))
                )
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .reduce("", (left, right) -> left + " " + right)
                .trim();
    }

    private List<String> copyList(List<String> source) {
        return source == null ? List.of() : List.copyOf(source);
    }

    private double roundScore(double score) {
        return Math.round(score * 100.0) / 100.0;
    }

    private List<OrganizationSearchDocument> loadSupplierDocuments() {
        try {
            List<OrganizationSearchDocument> documents = iterableToList(organizationSearchRepository.findAll());
            if (!documents.isEmpty()) {
                return documents;
            }
        } catch (Exception ex) {
            logger.warn("Organization search index could not be read. Falling back to database. Reason: {}", ex.getMessage());
        }

        return organizationRepository.findAll().stream()
                .map(this::mapToDocument)
                .peek(this::saveDocument)
                .toList();
    }

    private List<OrganizationSearchDocument> iterableToList(Iterable<OrganizationSearchDocument> documents) {
        List<OrganizationSearchDocument> result = new ArrayList<>();
        Iterator<OrganizationSearchDocument> iterator = documents.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private void saveDocument(OrganizationSearchDocument document) {
        try {
            organizationSearchRepository.save(document);
        } catch (Exception ex) {
            logger.warn("Organization search index save failed for org {}. Reason: {}",
                    document.getOrganizationId(), ex.getMessage());
        }
    }

    private OrganizationSearchDocument mapToDocument(Organization organization) {
        OrganizationSearchDocument document = new OrganizationSearchDocument();
        document.setId(organization.getId().toString());
        document.setOrganizationId(organization.getId());
        document.setRole(organization.getRole());
        document.setSupplierCategory(organization.getSupplierCategory());
        document.setName(organization.getName());
        document.setDescription(organization.getDescription());
        document.setLocation(organization.getLocation());
        document.setWebsite(organization.getWebsite());
        document.setVerified(organization.isVerified());
        document.setApproved(organization.isApproved());
        document.setOnboardingCompleted(organization.isOnboardingCompleted());
        document.setVisible(organization.isVisible());
        document.setFocusAreas(copyList(organization.getFocusAreas()));
        document.setCertifications(copyList(organization.getCertifications()));
        document.setReferenceProjects(copyList(organization.getReferenceProjects()));
        document.setExpertiseAreas(copyList(organization.getExpertiseAreas()));
        document.setHardwareCapabilities(copyList(organization.getHardwareCapabilities()));
        document.setSoftwareCapabilities(copyList(organization.getSoftwareCapabilities()));
        document.setRdCapabilities(copyList(organization.getRdCapabilities()));
        document.setProductionInfrastructure(copyList(organization.getProductionInfrastructure()));
        document.setTestInfrastructure(copyList(organization.getTestInfrastructure()));
        document.setServiceAreas(copyList(organization.getServiceAreas()));
        document.setTeamSize(organization.getTeamSize());
        document.setSearchText(buildSearchText(organization));
        return document;
    }
}
