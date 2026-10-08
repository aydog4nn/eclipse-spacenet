package org.dev.eclipse.service.impl;

import org.dev.eclipse.dto.response.WebsiteSuggestionResponse;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.enumarate.SupplierCategory;
import org.dev.eclipse.service.WebsiteSuggestionService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class WebsiteSuggestionServiceImpl implements WebsiteSuggestionService {

    @Override
    public WebsiteSuggestionResponse analyze(String website, Organization organization) {
        String normalizedText = normalizeWebsiteText(website) + " " + normalizeOrganizationText(organization);

        Set<String> focusAreas = new LinkedHashSet<>();
        Set<String> hardware = new LinkedHashSet<>();
        Set<String> software = new LinkedHashSet<>();
        Set<String> rd = new LinkedHashSet<>();
        Set<String> services = new LinkedHashSet<>();

        if (containsAny(normalizedText, "space", "uydu", "uzay", "satellite", "aerospace", "avionics")) {
            focusAreas.add("Uzay sistemleri");
        }
        if (containsAny(normalizedText, "rf", "haberlesme", "communication", "anten", "antenna")) {
            focusAreas.add("Haberlesme sistemleri");
            hardware.add("RF haberlesme");
        }
        if (containsAny(normalizedText, "pcb", "elektronik", "electronics", "sensor", "sensör")) {
            hardware.add("PCB tasarimi");
            hardware.add("Sensor entegrasyonu");
        }
        if (containsAny(normalizedText, "embedded", "gomulu", "firmware", "microcontroller")) {
            software.add("Gomulu yazilim");
        }
        if (containsAny(normalizedText, "ai", "artificial intelligence", "yapay zeka", "machine learning")) {
            software.add("Yapay zeka");
            rd.add("Veri analizi");
        }
        if (containsAny(normalizedText, "test", "validation", "verification", "dogrulama")) {
            services.add("Test hizmetleri");
        }
        if (containsAny(normalizedText, "prototype", "prototip", "manufacturing", "production", "uretim")) {
            services.add("Prototipleme");
            services.add("Uretim destegi");
        }
        if (containsAny(normalizedText, "research", "arge", "r&d")) {
            rd.add("Ar-ge faaliyetleri");
        }

        SupplierCategory suggestedCategory = suggestCategory(normalizedText);

        String summary = focusAreas.isEmpty() && hardware.isEmpty() && software.isEmpty() && rd.isEmpty() && services.isEmpty()
                ? "Website bazli belirgin bir teknik sinyal tespit edilemedi."
                : "Website ve kurum metinlerinden onboarding icin teknik etiket onerileri uretildi.";

        return new WebsiteSuggestionResponse(
                website,
                suggestedCategory,
                List.copyOf(focusAreas),
                List.copyOf(hardware),
                List.copyOf(software),
                List.copyOf(rd),
                List.copyOf(services),
                summary
        );
    }

    private SupplierCategory suggestCategory(String text) {
        if (containsAny(text, "startup", "venture", "innovation")) {
            return SupplierCategory.STARTUP;
        }
        if (containsAny(text, "manufacturing", "factory", "production", "uretim")) {
            return SupplierCategory.URETICI;
        }
        if (containsAny(text, "service", "consulting", "hizmet")) {
            return SupplierCategory.HIZMET_SAGLAYICI;
        }
        if (containsAny(text, "research", "arge", "r&d")) {
            return SupplierCategory.ARGE_FIRMASI;
        }
        if (containsAny(text, "sme", "kobi")) {
            return SupplierCategory.KOBI;
        }
        return null;
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeWebsiteText(String website) {
        if (website == null || website.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(website.contains("://") ? website : "https://" + website);
            return Stream.of(uri.getHost(), uri.getPath())
                    .filter(value -> value != null && !value.isBlank())
                    .map(value -> value.replace("www.", ""))
                    .map(value -> value.replaceAll("[^a-zA-Z0-9]+", " "))
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .reduce("", (left, right) -> left + " " + right)
                    .trim();
        } catch (Exception ex) {
            return website.toLowerCase(Locale.ROOT);
        }
    }

    private String normalizeOrganizationText(Organization organization) {
        if (organization == null) {
            return "";
        }
        return Stream.of(
                        organization.getName(),
                        organization.getDescription(),
                        organization.getActivityArea(),
                        organization.getWebsite(),
                        String.join(" ", organization.getFocusAreas())
                )
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .reduce("", (left, right) -> left + " " + right)
                .trim();
    }
}
