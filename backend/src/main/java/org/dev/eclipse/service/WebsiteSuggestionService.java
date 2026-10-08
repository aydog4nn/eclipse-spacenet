package org.dev.eclipse.service;

import org.dev.eclipse.dto.response.WebsiteSuggestionResponse;
import org.dev.eclipse.entity.Organization;

public interface WebsiteSuggestionService {

    WebsiteSuggestionResponse analyze(String website, Organization organization);
}
